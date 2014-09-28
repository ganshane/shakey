// Copyright 2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.internal

import java.io._
import scala.collection.mutable.Map
import shakey.services.{ShakeyErrorCode, ShakeyException, LoggerSupport}
import org.apache.commons.io.FileUtils
import org.apache.commons.io.input.ClassLoaderObjectInputStream
import scala.Some

/**
 * 实现本地存储用的KV数据库(NoSQL),主要存储本地化的设置.
 * 注意：  不可以频繁使用，
 * 不能用来承载业务数据
 */
class LocalSimpleStore(dir: String) extends LoggerSupport {
  //global store object
  private val _vs = new VersionedStore

  /**
   * get kv database snapshot
   * @return local database
   */
  private def snapshot: Map[Any, Any] = {
    //尝试10次读取snapshot
    var attempts: Int = 0
    while (true) {
      val latestPath = _vs.mostRecentVersionPath
      if (latestPath.isEmpty) return Map.empty[Any, Any]
      try {
        val bytes = FileUtils.readFileToByteArray(new File(latestPath.get))
        return deserialize(bytes).asInstanceOf[Map[Any, Any]]
      }
      catch {
        case e: IOException => {
          attempts += 1
          if (attempts >= 10) {
            throw ShakeyException.wrap(e, ShakeyErrorCode.FAIL_TO_GET_LOCAL_KV_SNAPSHOT)
          }
        }
      }
    }
    Map.empty[Any, Any]
  }

  /**
   * get value from kv database
   * @param key key
   * @tparam T value type
   * @return value
   */
  def get[T](key: Any): Option[T] = {
    return snapshot.get(key).asInstanceOf[Option[T]]
  }

  /**
   * put data to kv database
   * @param key key of data
   * @param value value
   */
  def put(key: Any, value: Any) {
    put(key, value, true)
  }

  /**
   * put data
   * @param key key of data
   * @param value value object
   * @param cleanup whether cleanup database
   */
  def put(key: Any, value: Any, cleanup: Boolean) {
    val curr: Map[Any, Any] = snapshot
    curr.put(key, value)
    persist(curr, cleanup)
  }

  /**
   * remove data with key given
   * @param key key be removed
   */
  def remove(key: Any) {
    remove(key, true)
  }

  def remove(key: Any, cleanup: Boolean) {
    val curr: Map[Any, Any] = snapshot
    curr.remove(key)
    persist(curr, cleanup)
  }

  def cleanup(keepVersions: Int) {
    _vs.cleanup(keepVersions)
  }

  private def persist(value: Map[Any, Any], cleanup: Boolean) {
    val toWrite: Array[Byte] = serialize(value)
    val newPath: String = _vs.createVersion
    FileUtils.writeByteArrayToFile(new File(newPath), toWrite)
    _vs.succeedVersion(newPath)
    //仅仅保存4个版本
    if (cleanup) _vs.cleanup(4)
  }

  private class VersionedStore {
    private final val FINISHED_VERSION_SUFFIX: String = ".version"
    assert(dir != null, "kv database directory is null!")
    private val _root: String = dir
    mkdirs(_root)

    def getRoot: String = {
      return _root
    }

    def versionPath(version: Long): String = {
      return new File(_root, "" + version).getAbsolutePath
    }

    def mostRecentVersionPath: Option[String] = {
      mostRecentVersion.map(versionPath(_))
    }

    def mostRecentVersionPath(maxVersion: Long): Option[String] = {
      val versionOpt = mostRecentVersion(maxVersion)
      versionOpt.map(versionPath(_))
    }

    def mostRecentVersion: Option[Long] = {
      getAllVersions.headOption
    }

    def mostRecentVersion(maxVersion: Long): Option[Long] = {
      getAllVersions.takeWhile(_ <= maxVersion).headOption
    }

    def createVersion: String = {
      val mostRecent = mostRecentVersion
      val version = mostRecent match {
        case Some(x) =>
          x + 1
        case None =>
          System.currentTimeMillis()
      }
      return createVersion(version)
    }

    def createVersion(version: Long): String = {
      val ret: String = versionPath(version)
      if (getAllVersions.contains(version))
        throw new ShakeyException(
          "Version already exists or data already exists",
          ShakeyErrorCode.VERSION_EXISTS_IN_KV)
      else return ret
    }

    def failVersion(path: String) {
      deleteVersion(validateAndGetVersion(path))
    }

    def deleteVersion(version: Long) {
      val versionFile: File = new File(versionPath(version))
      val tokenFile: File = new File(tokenPath(version))
      if (versionFile.exists) {
        FileUtils.forceDelete(versionFile)
      }
      if (tokenFile.exists) {
        FileUtils.forceDelete(tokenFile)
      }
    }

    def succeedVersion(path: String) {
      val version: Long = validateAndGetVersion(path)
      createNewFile(tokenPath(version))
    }

    def cleanup {
      cleanup(-1)
    }

    def cleanup(versionsToKeep: Int) {
      var versions: List[Long] = getAllVersions
      versions = versions.take(math.min(versions.size, versionsToKeep))

      listDir(_root).foreach {
        x =>
          val v: Long = parseVersion(x)
          if (v > 0 && !versions.contains(v)) {
            deleteVersion(v)
          }
      }
    }

    /**
     * Sorted from most recent to oldest
     */
    def getAllVersions: List[Long] = {
      listDir(_root).
        filter(_.endsWith(FINISHED_VERSION_SUFFIX)).
        map(validateAndGetVersion).
        sortWith(_.compareTo(_) > 0)
    }

    private def tokenPath(version: Long): String = {
      return new File(_root, "" + version + FINISHED_VERSION_SUFFIX).getAbsolutePath
    }

    private def validateAndGetVersion(path: String): Long = {
      val v: Long = parseVersion(path)
      if (v <= 0) throw new ShakeyException(path + " is not a valid version", ShakeyErrorCode.INVALID_LOCAL_VERSION)
      return v
    }

    private def parseVersion(path: String): Long = {
      var name: String = new File(path).getName
      if (name.endsWith(FINISHED_VERSION_SUFFIX)) {
        name = name.substring(0, name.length - FINISHED_VERSION_SUFFIX.length)
      }
      try {
        return java.lang.Long.parseLong(name)
      }
      catch {
        case e: NumberFormatException => {
          return -1L
        }
      }
    }

    private def createNewFile(path: String) {
      new File(path).createNewFile
    }

    private def mkdirs(path: String) {
      new File(path).mkdirs
    }

    private def listDir(dir: String): List[String] = {
      val contents = new File(dir).listFiles
      if (contents != null) {
        return contents.map(_.getAbsolutePath).toList
      }
      return List[String]()
    }

  }

  def serialize(obj: AnyRef): Array[Byte] = {
    try {
      val bos: ByteArrayOutputStream = new ByteArrayOutputStream
      val oos: ObjectOutputStream = new ObjectOutputStream(bos)
      oos.writeObject(obj)
      oos.close
      return bos.toByteArray
    }
    catch {
      case ioe: IOException => {
        throw new RuntimeException(ioe)
      }
    }
  }

  def deserialize(serialized: Array[Byte], loader: ClassLoader): AnyRef = {
    try {
      val bis: ByteArrayInputStream = new ByteArrayInputStream(serialized)
      var ret: AnyRef = null
      if (loader != null) {
        val cis: ClassLoaderObjectInputStream = new ClassLoaderObjectInputStream(loader, bis)
        ret = cis.readObject
        cis.close
      }
      else {
        val ois: ObjectInputStream = new ObjectInputStream(bis)
        ret = ois.readObject
        ois.close
      }
      return ret
    }
    catch {
      case ioe: IOException => {
        throw new RuntimeException(ioe)
      }
      case e: ClassNotFoundException => {
        throw new RuntimeException(e)
      }
    }
  }

  def deserialize(serialized: Array[Byte]): AnyRef = {
    return deserialize(serialized, Thread.currentThread().getContextClassLoader)
  }
}


