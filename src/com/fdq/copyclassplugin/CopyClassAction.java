package com.fdq.copyclassplugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fandaqian on 2018-01-18
 **/
public class CopyClassAction extends AnAction {

    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean isLinux = OS_NAME.toLowerCase().contains("linux") || OS_NAME.toLowerCase().contains("mac os x");

    private static final String TARGET = "target";
    private static final String SRC = "src";
    private static final String WEB_INF = "WEB-INF";
    private static final String SRC_MAIN_JAVA = SRC + FILE_SEP + "main" + FILE_SEP + "java";
    private static final String OUT_PRODUCTION = "out" + FILE_SEP + "production";
    private static final String TARGET_CLASS = TARGET + FILE_SEP + "classes";
    private static final String JAVA_EXT = ".java";
    private static final String CLASS_EXT = ".class";

    private static final String QQCYW = "qqcyw";
    private static final String QQCYM = "qqcym";
    private static final String QQCY_MOBILE = "qqcy-mobile";
    private static final String QQCY_MANAGER = "qqcy-manager";
    private static final String DATASERVICE = "dataService";

    /**
     * /home/isreal/ZZZ_IDEA_TEMP/
     * /home/isreal/ZZZ_IDEA_TEMP/qqcym/src/main/java/com/zf/qqcy/qqcym/service/AuthCasRealm.java
     * /home/isreal/ZZZ_IDEA_TEMP/qqcym/target/classes/com/zf/qqcy/qqcym/service/AuthCasRealm.class
     * qqcym/target/classes/com/zf/qqcy/qqcym/service/AuthCasRealm.class
     * /home/isreal/ZZZ_IDEA_TEMP/qqcym/src/main/java/com/zf/qqcy/qqcym/service/ClientUtils.java
     * /home/isreal/ZZZ_IDEA_TEMP/qqcym/target/classes/com/zf/qqcy/qqcym/service/ClientUtils.class
     * qqcym/target/classes/com/zf/qqcy/qqcym/service/ClientUtils.class
     * /home/isreal/ZZZ_IDEA_TEMP/qqcym/src/main/resources/application.properties
     * /home/isreal/ZZZ_IDEA_TEMP/qqcym/src/main/resources/application.properties
     * qqcym/src/main/resources/application.properties
     * /home/isreal/ZZZ_IDEA_TEMP/qqcyw/src/main/java/com/zf/qqcy/qqcyw/common/AppInit.java
     * /home/isreal/ZZZ_IDEA_TEMP/qqcyw/target/classes/com/zf/qqcy/qqcyw/common/AppInit.class
     * qqcyw/target/classes/com/zf/qqcy/qqcyw/common/AppInit.class
     **/

    @Override
    public void actionPerformed(AnActionEvent event) {
        if (isLinux) {
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            event.getPresentation().setVisible(true);
            event.getPresentation().setEnabled(editor != null);
            event.getPresentation().setIcon(AllIcons.General.Error);

            Project project = event.getData(PlatformDataKeys.PROJECT);
            DataContext dataContext = event.getDataContext();

            VirtualFile[] files = DataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            if (null != project && null != files && files.length > 0) {
                String location = getCPLocation();
                mkdir(location, Boolean.TRUE);
                String basePath = project.getBasePath().replaceAll(project.getName(), "");
                System.out.println(basePath);
                Map<String, List<String>> writeMap = new HashMap<>();
                for (VirtualFile file : files) {
                    if (!file.isDirectory()) {
                        System.out.println("===================================================");
                        System.out.println(file.getPath());
                        String module = "";
                        if (file.getPath().contains(QQCYW)) {
                            module = QQCYW;
                        } else if (file.getPath().contains(QQCYM)) {
                            module = QQCYM;
                        } else if (file.getPath().contains(QQCY_MOBILE)) {
                            module = QQCY_MOBILE;
                        } else if (file.getPath().contains(QQCY_MANAGER)) {
                            module = QQCY_MANAGER;
                        } else if (file.getPath().contains(DATASERVICE)) {
                            module = DATASERVICE;
                        }
                        mkdir(location + FILE_SEP + module, Boolean.FALSE);
                        if ("java".equals(file.getExtension())) {
                            makeJavaFile(file.getPath(), basePath, module, writeMap);
                        } else {
                            makeSrouceFile(file.getPath(), basePath, module, writeMap);
                        }
                    }
                }
                writeFile(writeMap);
            }
        }
    }

    private void makeJavaFile(String filePath, String basePath, String module, Map<String, List<String>> writeMap) {
        if (filePath.contains(SRC_MAIN_JAVA)) {
            filePath = filePath.replaceAll(SRC_MAIN_JAVA, TARGET_CLASS);
        } else {
            filePath = filePath.replaceAll(SRC, OUT_PRODUCTION + FILE_SEP + module);
        }
        filePath = filePath.replaceAll(JAVA_EXT, CLASS_EXT);
        String writeFilePath = filePath.replaceAll(basePath, "");
        writeFilePath = writeFilePath.replaceAll(TARGET, WEB_INF);
        System.out.println(filePath);
        System.out.println(writeFilePath);
        runProcess("cp", filePath, getCPLocation() + FILE_SEP + module);
        makeWriteMap(writeMap, module, writeFilePath);
    }


    private void makeSrouceFile(String filePath, String basePath, String module, Map<String, List<String>> writeMap) {
        String writeFilePath = filePath.replaceAll(basePath, "");
        System.out.println(filePath);
        System.out.println(writeFilePath);
        runProcess("cp", filePath, getCPLocation() + FILE_SEP + module);
        makeWriteMap(writeMap, module, writeFilePath);
    }

    private void mkdir(String location, boolean del) {
        if (del) {
            runProcess("rm", "-rf", location);
            runProcess("mkdir", location);
        } else {
            File file = new File(location);
            if (!file.exists()) {
                runProcess("mkdir", location);
            }
        }
    }

    private void runProcess(String... cmds) {
        StringBuilder builder = new StringBuilder();
        if (null != cmds) {
            for (String cmd : cmds) {
                builder.append(cmd).append(" ");
            }
        }
        System.out.println("command===>" + builder.toString());
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.redirectErrorStream(true);
        Process p;
        BufferedReader br = null;
        try {
            p = pb.start();
            String line;

            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isFileExits(String path) {
        if (isLinux) {
            return new File(path).exists();
        } else {
            try {
                return new File(new URI(path)).exists();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    }

    private boolean isBlank(String str) {
        return null == str || "".equals(str);
    }

    private boolean isNotBlank(String str) {
        return null != str && !"".equals(str);
    }

    private String getCPLocation() {
        return "/home/isreal/桌面/" + getCurrentDateTime();
    }

    private void makeWriteMap(Map<String, List<String>> writeMap, String module, String writeFilePath) {
        if (writeMap.containsKey(module)) {
            writeMap.get(module).add(writeFilePath);
        } else {
            List<String> list = new ArrayList<>();
            list.add(writeFilePath);
            writeMap.put(module, list);
        }
    }

    private void writeFile(Map<String, List<String>> writeMap) {
        if (null != writeMap && !writeMap.isEmpty()) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(getCPLocation() + FILE_SEP + "readme", false);
                for (Map.Entry<String, List<String>> entry : writeMap.entrySet()) {
                    fw.write(LINE_SEP);
                    List<String> list = entry.getValue();
                    for (String s : list) {
                        fw.write(s);
                        fw.write(LINE_SEP);
                    }
                }
                fw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != fw) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
