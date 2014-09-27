Name "Shakey Stock Scanner"
Caption "Shakey"
;Icon "Java Launcher.ico"
OutFile "shakey.exe"
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
 
Section ""
  Call GetJRE
  Pop $R0
 
  ; change for your purpose (-jar etc.)
  StrCpy $0 '"$R0" -cp ../lib/shakey-1.0-SNAPSHOT.jar -Dserver.home=.. shakey.app.ShakeyBootstrap'
 
  SetOutPath $EXEDIR
  ExecWait $0
SectionEnd
 
Function GetJRE 
  Push $R0
  Push $R1
 
  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""
 
  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound
 
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\javaw.exe"
 
  IfErrors 0 JreFound
  StrCpy $R0 "javaw.exe"
        
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd
