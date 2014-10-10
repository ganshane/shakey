// Copyright 2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.services;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * application bootstrap
 */
public class AppBootstrap {
    protected static void start(String mainClassName,String[] args) throws Exception{
        //get server home
        String serverHome = System.getProperty("server.home", "support");
        String LIB=serverHome+File.separator+"lib";

        List<URL> urls = new ArrayList<URL>();
        File [] files = new File(LIB).listFiles();

        if(files != null){
            for (File f : files) {
                urls.add(f.toURI().toURL());
            }
        }
        // feed your URLs to a URLClassLoader!
        ClassLoader classloader = new URLClassLoader(
                urls.toArray(new URL[urls.size()]),
                ClassLoader.getSystemClassLoader().getParent());

        // well-behaved Java packages work relative to the
        // context classloader.  Others don't (like commons-logging)
        Thread.currentThread().setContextClassLoader(classloader);

        // relative to that classloader, find the main class
        // you want to bootstrap, which is the first cmd line arg
        Class mainClass = classloader.loadClass(mainClassName);
        Method main = mainClass.getMethod("main",
                new Class[]{args.getClass()});

        main.invoke(null, new Object[] { args });
    }
}
