package com.jcnlp.spell.spellchecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 基于lucene ngram的拼写纠错功能demo
 * @author c_jia
 */
public class NgramSpellChecker {
  private static final String INDEX_STORE_DIR = "G:/index"; // 索引文件存储位置
  private static final String DIC_STORE_DIR = "G:/d.txt"; // 词典文件存储位置

  public static void main(String[] args) throws IOException {
    
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    Directory directory = FSDirectory.open(Paths.get(INDEX_STORE_DIR));
    
    // 采用NGram方式分词纠错
    // 还可以采用LevensteinDistance，JaroWinklerDistance等
    final SpellChecker sp = new SpellChecker(directory, new NGramDistance());
    BufferedReader reader = Files.newBufferedReader(Paths.get(DIC_STORE_DIR), Charset.forName("gbk"));
    sp.indexDictionary(new PlainTextDictionary(reader), config, true);
    
    // 测试1
    String[] suggestions = sp.suggestSimilar("保叔塔", 5);
    for (String word : suggestions) {
      System.out.println(word);
    }
    
    // 测试2
    suggestions = sp.suggestSimilar("tomorow", 5);
    for (String word : suggestions) {
      System.out.println(word);
    }
  }
  
}
