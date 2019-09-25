package wordcount;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 统计字符数、单词数和行数等
 */
public class WordCount {

    boolean isFuzzyQuery;
    boolean isCountChar;
    boolean isCountWord;
    boolean isCountLine;
    boolean isCountComplexLine;

    //创建线程池，并发任务
    public ExecutorService executor = Executors.newFixedThreadPool(3);

    /**
     * 统计一行中有多少字符
     */
    private int getCharCount(String line) {
        //空行算一个字符：“\n”
        if(line.isEmpty()) {
            return 1;
        }
        return line.length() + System.lineSeparator().length();
    }

    /**
     * 统计一行中有多少单词
     */
    private int getWorkCount(String line) {
        //把所有除了字母以外的字符都去掉
        line = line.replaceAll("[^a-zA-Z]", " ");
        //把多于两个以上的空格全部转化为一个空格
        line = line.replaceAll("\\s+"," ");
        //去掉首部和尾随的空格
        line = line.trim();
        //用空格分隔单词
        String[] words = line.split("[\\s+,.]");
        //如果为空行，则返回0
        if(words[0].equals("")) {
            return 0;
        }
        return words.length;
    }

    /**
     * 添加一行
     */
    private int addLineCount() {
        return 1;
    }

    /**
     * 判断是否是代码行，是则返回1
     */
    private int addCodeLine(String line) {
        if(addBlankLine(line) == 0 && addCommentLine(line) == 0) {
            return 1;
        }
        return 0;
    }

    /**
     * 判断是否是空白行，如果包括代码，则只有不超过一个可显示的字符，例如“{”。
     */
    private int addBlankLine(String line) {
        if(line.isEmpty()) return 1;
        if(!line.matches("[a-zA-Z]") && (line.trim().equals("{") || line.trim().equals("}"))) {
            return 1;
        }
        return 0;
    }

    /**
     * 判断是否是注释行，是则返回1
     */
    private int addCommentLine(String line) {
        line = line.trim();
        //匹配“//”单行注释或“} //”情况
        if(line.matches("}*\\s+//?.+")) {
            return 1;
        }
        //匹配“/**/”和“/** * */”的情况
        if(line.matches("((//?.+)|(/\\*+)|((^\\s)*\\*.+)|((^\\s)*\\*)|((^\\s)*\\*/))+")) {
            return 1;
        }
        return 0;
    }

    /**
     * 统计文件的字符数、单词数和行数等
     * @param fileName 文件名
     * @param strs 参数数组
     */
    public void count(String fileName,  String... strs) {
        checkParams(strs);
        if(isFuzzyQuery) {
            //递归处理文件
            countMultiFile(fileName);
        } else {
            //处理单文件
            countSingleFile(fileName, new CountResult(this));
        }
    }

    /**
     * 并发统计多文件
     * @param fileName
     */
    private void countMultiFile(String fileName) {
        File directory = new File("");  //设定为当前文件夹
        List<String> files = new ArrayList<>();
        FileUtil.findFiles(directory.getAbsolutePath(), fileName, files);
        //得到文件集合后，并发处理，提高效率
        if(files.size() == 0) {
            System.out.println("无法匹配到适合的文件");
            return;
        }
        for (String name: files) {
            executor.execute(() -> countSingleFile(name, new CountResult(this)));
        }
        //开启线程池执行任务后，关闭线程池释放资源
        executor.shutdown();
        try {
            boolean loop = true;
            while (loop) {
                loop = !executor.awaitTermination(2, TimeUnit.SECONDS);  //超时等待阻塞，直到线程池里所有任务结束
            } //等待所有任务完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 统计单文件
     * @param fileName
     * @param countCallback
     */
    private void countSingleFile(String fileName, Callback countCallback) {
        Count count = new Count();
        File file = new File(fileName);
        if(!file.exists()) {
            countCallback.onError("文件不存在，请重试");
            return;
        }
        BufferedReader reader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            String line;
            while ( null != (line = reader.readLine())){
                if(isCountLine)
                    count.lineCount = count.lineCount + addLineCount();
                if(isCountWord)
                    count.wordCount = count.wordCount + getWorkCount(line);
                if(isCountChar)
                    count.charCount = count.charCount + getCharCount(line);
                if(isCountComplexLine) {
                    count.codeLineCount = count.codeLineCount + addCodeLine(line);
                    count.blankLineCount = count.blankLineCount + addBlankLine(line);
                    count.commentLineCount = count.commentLineCount + addCommentLine(line);
                }
            }
            countCallback.onSuccess(count, fileName);
        } catch (IOException e) {
            countCallback.onError("文件读取出错，请重试");
        } finally {
            FileUtil.closeIOs(fileReader, reader);
        }
    }

    /**
     * 检查参数选择
     */
    private void checkParams(String... strs) {
        for (String str: strs) {
            switch (str) {
                case Constant.COUNT_CHAR:
                    isCountChar = true;
                    break;
                case Constant.COUNT_WORD:
                    isCountWord = true;
                    break;
                case Constant.COUNT_LINE:
                    isCountLine = true;
                    break;
                case Constant.COUNT_COMPLEX_LINE:
                    isCountComplexLine = true;
                    break;
                case Constant.MULTI_FILE_COUNT:
                    isFuzzyQuery = true;
                    break;
            }
        }
    }



    public interface Callback {
        void onError(String msg);
        void onSuccess(Count count, String fileName);
    }
}
