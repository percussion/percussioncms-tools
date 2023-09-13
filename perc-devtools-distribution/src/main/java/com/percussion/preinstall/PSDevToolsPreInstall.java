/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.preinstall;

import org.apache.tools.ant.taskdefs.Replace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PSDevToolsPreInstall {

        private static final String POM_PROPERTIES="/META-INF/maven/com.percussion/perc-devtools-distribution/pom.properties";
        public static final String DISTRIBUTION_DIR = "distribution";
        public static final String PERC_JAVA_HOME = "perc.java.home";
        public static final String JAVA_HOME = "java.home";
        public static final String PERCUSSION_VERSION = "project.version";
        public static final String INSTALL_TEMPDIR = "percInstallTmp_";
        public static final String PERC_ANT_JAR = "perc-ant";
        public static final String DEVELOPMENT = "DEVELOPMENT";
        public static final String ANT_INSTALL = "install.xml";
        public static final String JAVA_TEMP = "java.io.tmpdir";
        public static File tmpFolder;

        public static String developmentFlag = "false";
        public static String percVersion;
        public static volatile int currentLineNo;
        public static volatile int currentErrLineNo;
        public static volatile String debug="false";
        public static Integer processCode=0;
        public static Boolean error=false;

    private static String getVersionFromResource(){

            try (InputStream is = PSDevToolsPreInstall.class.getResourceAsStream(POM_PROPERTIES)) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("version","");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            return "";
    }

        /**
     * Find the ant jar by path pattern to avoid hard coding / forcing version.
     *
     * @param execPath Folder containing the jar
     * @param fileNameWithPattern A File name with a glob pattern like perc-ant-*.jar
     * @return Path to the ant jar
     * @throws IOException
     */
    private static Path getVersionLessJarFilePath(Path execPath, String fileNameWithPattern) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(execPath.toAbsolutePath(), fileNameWithPattern)) {
            List<Path> paths = new ArrayList<>();
            for (Path path : ds) {
                paths.add(path);
            }
            if (paths.isEmpty()) {
                throw new IOException(fileNameWithPattern + " not found.");
            } else if (paths.size() == 1) {
                return paths.get(0);
            } else {
                System.out.println("Warning: Multiple " + fileNameWithPattern + " jars found, selecting the first one: " + paths.get(0).toAbsolutePath().toString());
                return paths.get(0);
            }
        }
    }


    public static void main(String[] args) {
            try {

                if (args.length < 1) {
                    System.out.println("Must specify installation or upgrade folder");
                    System.exit(0);
                }

                Path installPath = Paths.get(args[0]);

                debug = System.getProperty("DEBUG");
                if(debug == null || debug.equalsIgnoreCase("")){
                    debug = "false";
                }

                String javaHome = System.getProperty(PERC_JAVA_HOME);
                if (javaHome == null || javaHome.trim().equalsIgnoreCase(""))
                    javaHome = System.getProperty(JAVA_HOME);

                String javabin = "";

                if (System.getProperty("file.separator").equals("/")) {
                    javabin = javaHome + "/bin/java";
                } else {
                    javabin = javaHome + "/bin/java.exe";
                }

                percVersion = System.getProperty(PERCUSSION_VERSION);
                if (percVersion == null || percVersion.equals(""))
                    percVersion = getVersionFromResource();

                developmentFlag = System.getProperty(DEVELOPMENT);
                if (developmentFlag == null || DEVELOPMENT.trim().equalsIgnoreCase(""))
                    developmentFlag = "false";

                System.out.println("perc.java.home=" + javaHome);
                System.out.println("java.bin=" + javabin);
                System.out.println("project.version=" + percVersion);
                System.out.println(DEVELOPMENT + "=" + developmentFlag);


                System.out.println("Installation folder is " + installPath.toAbsolutePath().toString());

                Path installSrc;
                Path currentJar = Paths.get(PSDevToolsPreInstall.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                if (!Files.isDirectory(currentJar)) {
                    installSrc = Files.createTempDirectory(INSTALL_TEMPDIR);

                    // add option to not delete for debugging
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override

                        public void run() {
                            //If the debug flag is set don't delete the files.
                            if(debug.equalsIgnoreCase("false")){
                                try {
                                    Files.walk(installSrc)
                                            .sorted(Comparator.reverseOrder())
                                            .map(Path::toFile)
                                            .forEach(File::delete);
                                } catch (IOException ex) {
                                    System.out.println("An error occurred while executing the installation, installation has likely failed. " + ex.getMessage());
                                }
                            }
                        }
                    });


                    extractArchive(currentJar, installSrc, DISTRIBUTION_DIR);



                } else {
                    System.out.println("Running from extracted jar");
                    installSrc = currentJar.resolve(DISTRIBUTION_DIR);
                }


                Path execPath = installSrc.resolve(Paths.get("rxconfig", "Installer"));
                Path installAntJarPath = execPath.resolve(getVersionLessJarFilePath(execPath,PERC_ANT_JAR + "-*.jar"));
                execJar(installAntJarPath, execPath, installPath);

            } catch (Exception e) {
                System.out.println("An error occurred while executing the installation, installation has likely failed. " + e.getMessage());
            }
            System.out.println("Done extracting");
        }


        public static void extractArchive(Path archiveFile, Path destPath, String folderPrefix) throws IOException {

            Files.createDirectories(destPath); // create dest path folder(s)

            try (ZipFile archive = new ZipFile(archiveFile.toFile())) {

                // sort entries by name to always create folders first
                List<? extends ZipEntry> entries = archive.stream()
                        .sorted(Comparator.comparing(ZipEntry::getName))
                        .collect(Collectors.toList());

                // copy each entry in the dest path
                for (ZipEntry entry : entries) {
                    currentLineNo++;
                    String entryName = entry.getName();
                    if (!entryName.startsWith(folderPrefix))
                        continue;

                    String name = entryName.substring(folderPrefix.length() + 1);
                    if (name.length() == 0)
                        continue;

                    Path entryDest = destPath.resolve(name);
                    File newFile = new File(entryDest.toString());
                    System.out.println("Unzipping to "+newFile.getAbsolutePath());
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();

                    if (entry.isDirectory()) {
                        Files.createDirectory(entryDest);
                        continue;
                    }

                    System.out.println("Creating file " + entryDest);
                    Files.copy(archive.getInputStream(entry), entryDest);


                }
            }   catch(Exception ex){
                ex.printStackTrace();
                error=true;
            }
        }

        public static Integer execJar(Path jar, Path execPath, Path installDir) throws IOException,
                InterruptedException {

            try {

                String javaHome = System.getProperty(PERC_JAVA_HOME);
                if (javaHome == null || javaHome.trim().equalsIgnoreCase(""))
                    javaHome = System.getProperty(JAVA_HOME);

                String javabin = "";

                if (System.getProperty("file.separator").equals("/")) {
                    javabin = javaHome + "/bin/java";
                } else {
                    javabin = javaHome + "\\bin\\java.exe";
                }

                //"-Dlistener=com.percussion.preinstall.AntBuildListener",
                ProcessBuilder builder = new ProcessBuilder(
                        javabin,"-Dproject.version="+percVersion,"-Dfile.encoding=UTF-8","-Dsun.jnu.encoding=UTF-8", "-Dinstall.dir=" + installDir.toAbsolutePath().toString(), "-jar", jar.toAbsolutePath().toString(), "-f", ANT_INSTALL).directory(execPath.toFile());

                //pass in known flags
                builder.environment().put(DEVELOPMENT, developmentFlag);
                builder.environment().put(PERCUSSION_VERSION, percVersion);

                //Pass on the temp dir if set
                builder.environment().put(JAVA_TEMP, System.getProperty("java.io.tmpdir"));
                Process process = builder.inheritIO().start();
                process.waitFor();

                processCode = process.exitValue();
                if(processCode!=0){
                    error=true;
                }
            }

            catch(Exception ex){
                ex.printStackTrace();
                processCode=-2;
                error=true;
            }
            return processCode;
        }

        public static void replaceTokens(File file, String replaceToken, String replaceValue){
            Replace r = new Replace();
            r.setFile(file);
            r.setToken(replaceToken);
            r.createReplaceToken();
            r.setValue(replaceValue);
            r.createReplaceValue();
            r.execute();
        }

        public static String getRunningJarVersion(){
            // static
            try {
                 URI file  = PSDevToolsPreInstall.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI();

                 String fileName = Paths.get(file).getFileName().toString();
                 String parts[] = fileName.split("^(.+?)-(\\d.*?)\\.jar$");
                if(parts.length==3)
                    return parts[2];
                else
                    return "";

            } catch (URISyntaxException e) {
                return "";
            }

        }
    }