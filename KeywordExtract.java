
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class KeywordExtract
{
	//first key is term, second key is length summaryNum titleNum #headNum #tailNum totalNum speech
	private HashMap<String,HashMap<String,Double>> termProp = new HashMap<String,HashMap<String,Double>>();
	//term number of this  article
	private Double termNum = 1.0;
	//the max length of term in this article 
	private Double lengthMax = 1.0;
	
	private String path_stopword = "C:\\data\\entity\\stopwords.txt";
	
	private Set<String> swSet = new HashSet<String>();
	/**
	 * 读取停用词
	 * @throws IOException
	 */
	private void readStopwords() throws IOException
	{
		// 读取文件流 读入结果集
		FileReader reader = null;
		try
		{
			reader = new FileReader(path_stopword);// 读取url文件
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader alines = new BufferedReader(reader);
		String s1 = null;
		while ((s1 = alines.readLine()) != null)
		{
			swSet.add(s1.trim());
		}
		reader.close();

	}
	/**
	 * 
	 * @param summary
	 * @param title
	 * @param content
	 */
	private void statistics(String summary, String title, String content)
	{
		String[] summaryTerms = summary.split(" ");
		
		String[] titleTerms = title.split(" ");
		
		String[] contentTerms = content.split(" ");
		//处理摘要中的词
		for(String summaryTerm : summaryTerms)
		{
			if (summaryTerm.contains("_w") || summaryTerm.contains("_t") || summaryTerm.contains("_c") || summaryTerm.contains("_f") || summaryTerm.contains("_m") || summaryTerm.contains("_mq") || summaryTerm.contains("_o")
					|| summaryTerm.contains("_p") || summaryTerm.contains("_q") || summaryTerm.contains("_r") || summaryTerm.contains("_s") || summaryTerm.contains("_u") || summaryTerm.contains("_y"))
			{
				// nothing
			}
			else
			{
				if(summaryTerm.contains("_"))
				{
					String[] term = summaryTerm.split("_");
					String t = term[0].trim().toLowerCase();
					String s = term[1];
					if(swSet.contains(t)||t.length()==1)
					{
						continue;
					}
					if(termProp.containsKey(t))
					{
						HashMap<String,Double> termMap = termProp.get(t);
						termMap.put("summaryNum", termMap.get("summaryNum")+1);
						termMap.put("totalNum", termMap.get("totalNum")+1);
						termProp.put(t, termMap);
					}
					else
					{
						Double speechValue = getSpeechWeight(t,s);
						HashMap<String,Double> termMap = new HashMap<String,Double>();
						Double  termLength = Double.valueOf(t.length());
						termMap.put("length", termLength);
						termMap.put("summaryNum", 1.0);
						termMap.put("titleNum", 0.0);
						termMap.put("totalNum", 1.0);
						termMap.put("speech", speechValue);
						termProp.put(t, termMap);
						
						getLenMax(termLength);
					}
				}
				else
				{
					continue;
				}
			}
			
		}
		//处理标题中的词
		for(String titleTerm : titleTerms)
		{
			if (titleTerm.contains("_w") || titleTerm.contains("_t") || titleTerm.contains("_c") || titleTerm.contains("_f") || titleTerm.contains("_m") || titleTerm.contains("_mq") || titleTerm.contains("_o")
					|| titleTerm.contains("_p") || titleTerm.contains("_q") || titleTerm.contains("_r") || titleTerm.contains("_s") || titleTerm.contains("_u") || titleTerm.contains("_y"))
			{
				// nothing
			}
			else
			{
				if(titleTerm.contains("_"))
				{
					String[] term = titleTerm.split("_");
					String t = term[0].trim().toLowerCase();
					String s = term[1];
					if(swSet.contains(t)||t.length()==1)
					{
						continue;
					}
					if(termProp.containsKey(t))
					{
						HashMap<String,Double> termMap = termProp.get(t);
						termMap.put("totalNum", termMap.get("totalNum")+1);
						termMap.put("titleNum", termMap.get("titleNum")+1);
						termProp.put(t, termMap);
					}
					else
					{
						Double speechValue = getSpeechWeight(t,s);
						HashMap<String,Double> termMap = new HashMap<String,Double>();
						Double  termLength = Double.valueOf(t.length());
						termMap.put("length", termLength);
						termMap.put("summaryNum", 0.0);
						termMap.put("titleNum", 1.0);
						termMap.put("totalNum", 1.0);
						termMap.put("speech", speechValue);
						termProp.put(t, termMap);
						
						getLenMax(termLength);
					}
				}
				else
				{
					continue;
				}
			}
				
		}
		//处理正文中的词
		for(String contentTerm : contentTerms)
		{
			if (contentTerm.contains("_w") || contentTerm.contains("_t") || contentTerm.contains("_c") || contentTerm.contains("_f") || contentTerm.contains("_m") || contentTerm.contains("_mq") || contentTerm.contains("_o")
					|| contentTerm.contains("_p") || contentTerm.contains("_q") || contentTerm.contains("_r") || contentTerm.contains("_s") || contentTerm.contains("_u") || contentTerm.contains("_y"))
			{
				// nothing
			}
			else
			{
				if(contentTerm.contains("_"))
				{
					
					String[] term = contentTerm.split("_");
					String t = term[0].trim().toLowerCase();
					String s = term[1];
					if(swSet.contains(t)||t.length()==1)
					{
						continue;
					}
					if(termProp.containsKey(t))
					{
						HashMap<String,Double> termMap = termProp.get(t);
						termMap.put("totalNum", termMap.get("totalNum")+1);
						termProp.put(t, termMap);
					}
					else
					{
						Double speechValue = getSpeechWeight(t,s);
						HashMap<String,Double> termMap = new HashMap<String,Double>();
						Double  termLength = Double.valueOf(t.length());
						termMap.put("length", termLength);
						termMap.put("summaryNum", 0.0);
						termMap.put("titleNum", 0.0);
						termMap.put("totalNum", 1.0);
						termMap.put("speech", speechValue);
						termProp.put(t, termMap);
						
						getLenMax(termLength);
					}
					
				}
				else
				{
					continue;
				}
			}
		}
		//文章中出现的词数（不重复的）
		termNum = Double.valueOf(termProp.size());
		
	}
	/**
	 * 获取文章中最大词长度，最大值为10
	 * @param termLen
	 */
	private void getLenMax(Double termLen)
	{
		if(lengthMax < termLen)
		{
			lengthMax = termLen;
		}
		if(lengthMax > 10)
		{
			lengthMax = 10.0;
		}
	}
	/**
	 * 计算词性权重
	 * @param t 词
	 * @param s 词性
	 * @return
	 */
	private Double getSpeechWeight(String t,String s)
	{
		//自定义词库
		if(t.length()>=2 && s.equals("x"))
		{
			return 1.0;
		}
		//n名词  nr人名  ns地名  nt机构名  nz专有名词  e产品词 
		else if(s.equals("n")||s.equals("nr")||s.equals("ns")||s.equals("nt")||s.equals("nz")||s.equals("e"))
		{
			return 0.8;
		}
		//v动词 i成语  l习语
		else if(s.equals("v")||s.equals("i")||s.equals("l"))
		{
			return 0.6;
		}
		//d副词 a形容词
		else if(s.equals("d")||s.equals("a"))
		{
			return 0.2;
		}
		else
			return 0.0;
	}
	/**
	 * 根据词频计算权重
	 * @param totalNum
	 * @return
	 */
	private Double getFreqWeight(Double totalNum)
	{
		Double freqWei = 0.0;
		freqWei = totalNum /(1+totalNum);
		return freqWei;
	}
	/**
	 * 根据词的长度计算权重
	 * @param TermLen
	 * @return
	 */
	private Double getLenthWeight(Double TermLen)
	{
		Double lenWei = 0.0;
		lenWei = TermLen / lengthMax;
		return lenWei;
	}
	
	/**
	 * 根据词的位置计算权重
	 * @param summaryNum
	 * @param titleNum
	 * @return
	 */
	private Double getLocationWeight(Double summaryNum, Double titleNum)
	{
		Double locWei = 0.0;
		locWei = 10 * (titleNum * 5 + summaryNum * 3) / termNum;
		return locWei;
	}
	/**
	 * 根据历史统计获得权重  尚未开通此功能
	 * @param hisFreq
	 * @param totalNum
	 * @return
	 */
	private Double getHistoryWeight(Double hisFreq, Double totalNum)
	{
		//历史词库的词总数 在一定时期内是个固定值
		Double hisNum = 1.0;
		Double hisWei = 0.0;
		hisWei = hisFreq / hisNum * totalNum;
		return hisWei;
	}
	/**
	 * 利用比较器给map按照value值排序
	 * @param map
	 * @return
	 */
	private List<Map.Entry<String, Double>> mapSort(Map<String, Double> map)
	{
		List<Map.Entry<String, Double>> mappingList = null;

		// 通过ArrayList构造函数把map.entrySet()转换成list
		mappingList = new ArrayList<Map.Entry<String, Double>>(map.entrySet());
		// 通过比较器实现比较排序
		Collections.sort(mappingList, new Comparator<Map.Entry<String, Double>>()
		{
			public int compare(Map.Entry<String, Double> mapping1, Map.Entry<String, Double> mapping2)
			{
				return mapping2.getValue().compareTo(mapping1.getValue());
			}
		});

		// for(Map.Entry<String,Double> mapping:mappingList){
		// System.out.println(mapping.getKey()+":"+mapping.getValue());
		// }
		return mappingList;
	}
	/**
	 * 
	 * @param summary
	 * @param title
	 * @param content
	 */
	public List<Map.Entry<String, Double>> calWeight(String summary, String title, String content)
	{
		try
		{
			readStopwords();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String,Double> termWeiMap = new HashMap<String,Double>();
		statistics( summary, title, content);
		for(Entry<String,HashMap<String,Double>> entry : termProp.entrySet())
		{
			HashMap<String,Double> termMap = entry.getValue();
			Double weight = 0.0;
			Double freqWei = getFreqWeight(termMap.get("totalNum"));
			Double lenWei = getLenthWeight(termMap.get("length")); 
			Double speWei = termMap.get("speech");
			Double locWei = getLocationWeight(termMap.get("summaryNum"),termMap.get("titleNum"));
			//Double hisWei = getHistoryWeight();  //目前尚未开通此功能
			weight = 1.5 * freqWei + 1.2 * speWei + locWei + 0.7 * lenWei;//hisWei
			termWeiMap.put(entry.getKey(), weight);		
		}
		List<Map.Entry<String, Double>> termWeiList = mapSort(termWeiMap);
		return termWeiList;
	}
}
