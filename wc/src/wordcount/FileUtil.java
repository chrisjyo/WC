package wordcount;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileUtil {

    /**
     * 递归查找匹配的文件
     * @param dirName 某个目录下
     * @param fileName 待匹配的文件（可带通配符）
     * @param files 存放匹配成功的文件名
     */
    public static void findFiles(String dirName, String fileName, List<String> files) {
        String tempName;
        //判断目录是否存在
        File baseDir = new File(dirName);
        if (!baseDir.exists() || !baseDir.isDirectory()){
            System.out.println("找不到该目录：" + dirName );
        } else {
            String[] fileList = baseDir.list();
            if(fileList == null) return;
            for (String s : fileList) {
                File readFile = new File(dirName + "\\" + s);
                if (!readFile.isDirectory()) {
                    tempName = readFile.getName();
                    if (wildcardMatch(fileName, tempName)) {
                        //匹配成功，将文件名添加到文件列表中
                        files.add(readFile.getAbsoluteFile().getAbsolutePath());
                    }
                } else if (readFile.isDirectory()) {
                    findFiles(dirName + "\\" + s, fileName, files);
                }
            }
        }
    }

    /**
     * 通配符匹配（参考网上）
     * @param pattern 通配符模式
     * @param str 待匹配的字符串
     * @return  匹配成功则返回true，否则返回false
     */
    private static boolean wildcardMatch(String pattern, String str) {
        int patternLength = pattern.length();
        int strLength = str.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                //通配符星号*表示可以匹配任意多个字符
                while (strIndex < strLength) {
                    if (wildcardMatch(pattern.substring(patternIndex + 1),
                            str.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else if (ch == '?') {
                //通配符问号?表示匹配任意一个字符
                strIndex++;
                if (strIndex > strLength) {
                    //表示str中已经没有字符匹配?了。
                    return false;
                }
            } else {
                if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return (strIndex == strLength);
    }

    /**
     * 关闭IO流
     */
    public static void closeIOs(Closeable... closeables) {
        for (Closeable closeable:closeables) {
            if(closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
