package wordcount;

public class Main {

    /**
     * 程序入口
     * @param args 第一参数：{-c, -w, -l, -s, -a}；第二参数：[fileName]
     */
    public static void main(String[] args) {
        WordCount wordCount = new WordCount();
        if (args == null || args.length == 0) {
            System.out.println("需要参数{-c, -w, -l, -s, -a} [fileName]，请重新输入");
        } else {
            wordCount.count(args[args.length - 1], args);
        }
    }
}
