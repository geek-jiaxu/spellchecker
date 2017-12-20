
# 拼写纠错功能

本文基于lucene6.1中的spellchcker编写的使用示例，并介绍功能实现流程,实现类似以下百度中的纠错

![Alt text](/Image.png)

首先我们要有一份词典数据，作为正确的词，供lucene建立索引，词典可参考file/dictionary.txt文件格式

### 1. INDEX_STORE_DIR倒排索引存储路径
``
Analyzer analyzer = new StandardAnalyzer();   <br />
IndexWriterConfig config = new IndexWriterConfig(analyzer);   
Directory directory = FSDirectory.open(Paths.get(INDEX_STORE_DIR));
``
### 2. 采用NGram方式分词纠错， ngram相关基础知识可以参见http://m.blog.csdn.net/baimafujinji/article/details/51281816，还可以采用LevensteinDistance，JaroWinklerDistance等
`` 
    final SpellChecker sp = new SpellChecker(directory, new NGramDistance());
    BufferedReader reader = Files.newBufferedReader(Paths.get(DIC_STORE_DIR), Charset.forName("gbk"));
    sp.indexDictionary(new PlainTextDictionary(reader), config, true);
``    
    以Today为例，看一下lucene索引里存储了怎样的数据
    
    至于n的取值，参考以下两个方法，l为词的长度
    ``
    private static int getMin(int l) {
        if (l > 5) {
          return 3;
        }
        if (l == 5) {
          return 2;
        }
        return 1;
      }

      private static int getMax(int l) {
        if (l > 5) {
          return 4;
        }
        if (l == 5) {
          return 3;
        }
        return 2;
      }
    ``
    构造Document
    首先``Field f = new StringField(F_WORD, text, Field.Store.YES);``会创建word字段，保存“Today”词原值，用于查询时返回
    其次构建gram{getMin<=n<=getMax}字段，例如n=2时，构建的gram2字段存储的值为To,od,da,ay，当然若是单词开头还会构建start2字段取值为To，单词结尾还会构建end2字段取值ay
  
    单词Today(2<=n<=3)索引中的存储内容如下:
    
    Filed   |  Values
    --------------------------------------------
    word    |  Today
    start2  |  To
    ngram2  |  To,od,da,ay
    end2    |  ay
    start3  |  Tod
    ngram3  |  Tod,oda,day
    end3    |  day
    
#### 3. 测试
    以“Todey”为例介绍检索过程
    检索时同样会生成Todey的所有ngram片段，最终构造的query如下
    (start2:To)^2.0 (end2:ey)^1.0 gram2:To gram2:od gram2:de gram2:ey (start3:Tod)^2.0 (end3:dey)^1.0 gram3:Tod gram3:ode gram3:dey
    
    召回所有结果后，会计算召回的单词与Todey的ngram-distance，若大于等于DEFAULT_ACCURACY = 0.5f精度才进行返回，精度可以通过suggestSimilar(String word, int numSug, float accuracy)方法传入自定义的取值
    
    最终返回结果:Today，Tomorrow
