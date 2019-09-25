package wordcount;

public class CountResult implements WordCount.Callback {

    private WordCount wordCount;

    public CountResult(WordCount wordCount) {
        this.wordCount = wordCount;
    }

    @Override
    public void onError(String msg) {
        System.out.println(msg);
    }

    @Override
    public void onSuccess(Count count, String fileName) {
        System.out.println("文件名为： " + fileName);
        if(wordCount.isCountChar) System.out.println("字符总数为：" + count.charCount);
        if(wordCount.isCountWord) System.out.println("单词总数为：" + count.wordCount);
        if(wordCount.isCountLine) System.out.println("行总数为：" + count.lineCount);
        if(wordCount.isCountComplexLine) {
            System.out.println("代码行总数为：" + count.codeLineCount);
            System.out.println("空白行总数为：" + count.blankLineCount);
            System.out.println("注释行总数为：" + count.commentLineCount);
        }
    }
}
