package 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * <PRE>
 * 作用 : 从文章中选取关键词（topic）
 *   
 *   		考虑词的位置，词的长度，词性
 * 使用 :输入标题，内容，摘要，停用词表进行计算，格式如下 
 *   
 * 示例 :List<Map.Entry<String, Double>> calWeight(String summary, String title, String content,Set<String> swSet)
 *   
 * 注意 :返回一个最大长度为10的list
 * 	 
 * 历史 :最基础的版本只有一些基于统计特征的计算，新版本特性增加了基于规则的计算
 * 	例如对于引号，冒号，和书名号等文中的特殊字段进行应用，删去了一些表达不明确的词性，仅留下了实体名词
 * 	并使用特殊标记加以区分，方便后续的处理
 * </PRE>
 */
public class KeywordExtract
{
	private static KeywordExtract instance = new KeywordExtract();

	public static KeywordExtract getKeywordExtract()
	{
		return instance;
	}


	public KeywordExtract()
	{
	}

	/**
	 * 
	 * @param s_content
	 * @return
	 */
	public String scnt2cnt(String s_content)
	{
		if(s_content == null || s_content.isEmpty())
			return null;
		String content = s_content.replaceAll(" *_[a-zA-Z]+ *", "");
		return content;
	}
	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public int strTimes(String s, String content)
	{
		if(s == null || content == null)
			return 0;
		int count = 0;
		int start = 0;
		while(content.indexOf(s,start) >= 0 && start < content.length())
		{
			count++;
			start = content.indexOf(s,start) + s.length();
		}
		return count;
	}
	/**
	 * 提取书名号内部的内容 优先级最高
	 * 长度大于1小于等于12
	 * @param s
	 * @return
	 */
	public HashMap<String,Double> extractBookmark(String s)
	{
		HashMap<String,Double> wordMap = new HashMap<String,Double>();
		Pattern pattern = Pattern.compile("《(.+?)》");//\"(.+)\"|
		Matcher matcher = pattern.matcher(s);
		while(matcher.find())
		{
			if(matcher.group(1).length() > 1&&matcher.group(1).length() <= 12)
			{
				if(wordMap.containsKey(matcher.group(1)))
				{
					wordMap.put(matcher.group(1), wordMap.get(matcher.group(1))+1);
				}
				else
				{
					wordMap.put(matcher.group(1), 1.0);
				}
			}
		}
		return wordMap;
	}
	/**
	 * 提取引号内部的内容 优先级中等
	 * 长度大于1小于等于6
	 * @param s
	 * @return
	 */
	public HashMap<String,Double> extractQuo(String title,String content)
	{
		HashMap<String,Double> quMap = new HashMap<String,Double>();
		Pattern pattern = Pattern.compile("“(.+?)”");//\"(.+)\"|
		Matcher matcher = pattern.matcher(title);
		while(matcher.find())
		{
			if(matcher.group(1).length() > 1&&matcher.group(1).length() <= 6)
			{
				String rs = filter(matcher.group(1));
				int count = strTimes(rs,content);
				quMap.put(rs, Double.valueOf(count));
			}
		}
		return quMap;
	}
	/**
	 * 提取冒号前面的部分，一般为人名，或者来源
	 * 优先级最低
	 * 长度大于1小于等于5
	 * @param s
	 * @return
	 */
	public HashMap<String,Double> extractColon(String title,String content)
	{
		HashMap<String,Double> colMap = new HashMap<String,Double>();
		if(title.contains("："))
		{
			String temp1 = title.split("：")[0];
			if(temp1.contains(" "))
			{
				String temp2 = temp1.split(" ")[temp1.split(" ").length-1];
				temp2 =  filter(temp2);
				if(temp2.length()>1 && temp2.length()<=5)
				{
					int count = strTimes(temp2,content);
					colMap.put(temp2, Double.valueOf(count));
				}				
			}
			else
			{
				temp1 =  filter(temp1);
				if(temp1.length()>=1 && temp1.length()<=5)
				{
					int count = strTimes(temp1,content);
					colMap.put(temp1, Double.valueOf(count));
				}
			}
		}
		else
		{
//			return wordSet;
		}
		return colMap;
	}
	private String filter(String s)
	{
		String rs = s;
		rs = rs.replaceAll("“", "").replaceAll("”", "").replaceAll("《", "").replaceAll("》", "");
		return rs;
	}
	/**
	 * 
	 * @param summary
	 * @param title
	 * @param content
	 */
	private HashMap<String, HashMap<String, Double>> statistics(String s_summary, String s_title, String s_content)// ,Set<String>
																													// swSet
	{
		// first key is term, 
		//second key is length summaryNum titleNum #headNum
		//type 1书名号kb 3引号kq 5冒号ks 7地名机构名专名kl 11人名实体词kr
		// #tailNum totalNum speech
		HashMap<String, HashMap<String, Double>> termProp = new HashMap<String, HashMap<String, Double>>();
		String title = null;
		String summary = null;
		String content = null;
		
		String[] titleTerms = null;
		if (s_title != null)
		{
			title = scnt2cnt(s_title);
			
			titleTerms = s_title.split(" ");
			// 处理标题中的词
			for (String titleTerm : titleTerms)
			{
				if (titleTerm.contains("_x") || titleTerm.contains("_nr") || titleTerm.contains("_ns") || titleTerm.contains("_nt") || titleTerm.contains("_nz") )
				{
					if (titleTerm.length() != 1)
					{
						String[] term = titleTerm.split("_");
						String t = term[0].trim().toLowerCase();
						if(t.length()<=3 && (t.startsWith("老")||t.startsWith("小")||t.contains("某")))
							continue;
						String s = term[1];
						if (t.length() == 1)
						{
							continue;
						}
						if (termProp.containsKey(t))
						{
							HashMap<String, Double> termMap = termProp.get(t);
							termMap.put("totalNum", termMap.get("totalNum") + 1);
							termMap.put("titleNum", termMap.get("titleNum") + 1);
							termProp.put(t, termMap);
						}
						else
						{
							Double speechValue = getSpeechWeight(t, s);
							HashMap<String, Double> termMap = new HashMap<String, Double>();
							Double termLength = Double.valueOf(t.length());
							termMap.put("length", termLength);
							termMap.put("summaryNum", 0.0);
							termMap.put("titleNum", 1.0);
							termMap.put("totalNum", 1.0);
							termMap.put("speech", speechValue);
							if(s.equals("x")||s.equals("nr"))
							{
								termMap.put("type", 11.0);
							}
							else if(s.equals("ns")||s.equals("nt")||s.equals("nz"))
							{
								termMap.put("type", 7.0);
							}
							termProp.put(t, termMap);
							// getLenMax(termLength);
						}
					}
					else
					{
						continue;
					}
				}
				else
				{
					// nothing
				}
			}
		}
		
		String[] summaryTerms = null;
		if (s_summary != null)
		{
			summary = scnt2cnt(s_summary);
			summaryTerms = s_summary.split(" ");
			// 处理摘要中的词
			for (String summaryTerm : summaryTerms)
			{
				if (summaryTerm.contains("_x") || summaryTerm.contains("_nr") || summaryTerm.contains("_ns") || summaryTerm.contains("_nt") || summaryTerm.contains("_nz"))//|| summaryTerm.contains("_e")
				{
					if (summaryTerm.length() != 1)
					{
						String[] term = summaryTerm.split("_");
						String t = term[0].trim().toLowerCase();
						if(t.length()<=3 && (t.startsWith("老")||t.startsWith("小")||t.contains("某")))
							continue;
						String s = term[1];
						if (t.length() == 1)
						{
							continue;
						}
						if (termProp.containsKey(t))
						{
							HashMap<String, Double> termMap = termProp.get(t);
							termMap.put("summaryNum", termMap.get("summaryNum") + 1);
							termMap.put("totalNum", termMap.get("totalNum") + 1);
							termProp.put(t, termMap);
						}
						else
						{
							Double speechValue = getSpeechWeight(t, s);
							HashMap<String, Double> termMap = new HashMap<String, Double>();
							Double termLength = Double.valueOf(t.length());
							termMap.put("length", termLength);
							termMap.put("summaryNum", 1.0);
							termMap.put("titleNum", 0.0);
							termMap.put("totalNum", 1.0);
							termMap.put("speech", speechValue);
							if(s.equals("x")||s.equals("nr"))
							{
								termMap.put("type", 11.0);
							}
							else if(s.equals("ns")||s.equals("nt")||s.equals("nz"))
							{
								termMap.put("type", 7.0);
							}
							termProp.put(t, termMap);
							// getLenMax(termLength);
						}
					}
					else
					{
						continue;
					}
				}
				else
				{
					//nothing
				}
			}
		}
		

		String[] contentTerms = null;
		if (s_content != null)
		{
			contentTerms = s_content.split(" ");
			// 处理正文中的词
			for (String contentTerm : contentTerms)
			{
				if (contentTerm.contains("_x") || contentTerm.contains("_nr") || contentTerm.contains("_ns") || contentTerm.contains("_nt") || contentTerm.contains("_nz")
						)//|| contentTerm.contains("_e")
				{
					if (contentTerm.length() != 1)
					{
						String[] term = contentTerm.split("_");
						String t = term[0].trim().toLowerCase();
						if(t.length()<=3 && (t.startsWith("老")||t.startsWith("小")||t.contains("某")))
							continue;
						String s = term[1];
						if (t.length() == 1)
						{
							continue;
						}
						if (termProp.containsKey(t))
						{
							HashMap<String, Double> termMap = termProp.get(t);
							termMap.put("totalNum", termMap.get("totalNum") + 1);
							termProp.put(t, termMap);
						}
						else
						{
							Double speechValue = getSpeechWeight(t, s);
							HashMap<String, Double> termMap = new HashMap<String, Double>();
							Double termLength = Double.valueOf(t.length());
							termMap.put("length", termLength);
							termMap.put("summaryNum", 0.0);
							termMap.put("titleNum", 0.0);
							termMap.put("totalNum", 1.0);
							termMap.put("speech", speechValue);
							if(s.equals("x")||s.equals("nr"))
							{
								termMap.put("type", 11.0);
							}
							else if(s.equals("ns")||s.equals("nt")||s.equals("nz"))
							{
								termMap.put("type", 7.0);
							}
							termProp.put(t, termMap);
							// getLenMax(termLength);
						}
					}
					else
					{
						continue;
					}
				}
				else
				{
					// nothing
				}
			}
		}
		String bmStr = title + summary + content;
		HashMap<String,Double> bmMap = extractBookmark(bmStr);
		if(bmMap.size() >= 1)
		{
			for(Entry<String,Double> entry : bmMap.entrySet())
			{
				if(termProp.containsKey(entry.getKey()))
				{
					HashMap<String, Double> termMap = termProp.get(entry.getKey());
					termMap.put("type", 1.0);
					termProp.put(entry.getKey(), termMap);
				}
				else
				{
					HashMap<String, Double> termMap = new HashMap<String, Double>();
					Double termLength = Double.valueOf(entry.getKey().length());
					termMap.put("length", termLength);
					termMap.put("summaryNum", 0.0);
					termMap.put("titleNum", entry.getValue());
					termMap.put("totalNum", entry.getValue());
					termMap.put("speech", 2.0);
					termMap.put("type", 1.0);
					termProp.put(entry.getKey(), termMap);
				}
			}
		}
		// 文章中出现的词数（不重复的）
		// termNum = Double.valueOf(termProp.size());
		return termProp;
	}
	/**
	 * 获取文章中最大词长度，最大值为10
	 * 
	 * @param termLen
	 */
	private Double getLenMax(HashMap<String, HashMap<String, Double>> termProp)
	{
		Double lengthMax = 1.0;
		for (Entry<String, HashMap<String, Double>> e : termProp.entrySet())
			if (lengthMax < e.getKey().length())
			{
				lengthMax = (double) e.getKey().length();
			}
		if (lengthMax > 10)
		{
			lengthMax = 10.0;
		}
		return lengthMax;
	}

	/**
	 * 计算词性权重
	 * 
	 * @param t
	 *            词
	 * @param s
	 *            词性
	 * @return
	 */
	private Double getSpeechWeight(String t, String s)
	{
		// x自定义词库nr人名
		if (t.length() >= 2 && (s.equals("x") ))
		{
			return 1.0;
		}
		//  ns地名 nt机构名 nz专有名词
		else if (s.equals("ns") || s.equals("nr")|| s.equals("nt") || s.equals("nz") )
		{
			return 0.8;
		}
		else
			return 0.0;
	}

	/**
	 * 根据词频计算权重
	 * 
	 * @param totalNum
	 * @return
	 */
	private Double getFreqWeight(Double totalNum)
	{
		Double freqWei = 0.0;
		freqWei = totalNum / (1 + totalNum);
		return freqWei;
	}

	/**
	 * 根据词的长度计算权重
	 * 
	 * @param TermLen
	 * @return
	 */
	private Double getLenthWeight(Double TermLen, Double lengthMax)
	{
		Double lenWei = 0.0;
		if (TermLen > 10)
		{
			TermLen = 10.0;
		}
		lenWei = TermLen / lengthMax;
		return lenWei;
	}

	/**
	 * 根据词的位置计算权重
	 * 
	 * @param summaryNum
	 * @param titleNum
	 * @return
	 */
	private Double getLocationWeight(Double summaryNum, Double titleNum, Double termNum)
	{
		Double locWei = 0.0;
		locWei = 10 * (titleNum * 5 + summaryNum * 3) / termNum;
		return locWei;
	}

	/**
	 * 根据历史统计获得权重 尚未开通此功能
	 * 
	 * @param hisFreq
	 * @param totalNum
	 * @return
	 */
	private Double getHistoryWeight(Double hisFreq, Double totalNum)
	{
		// 历史词库的词总数 在一定时期内是个固定值
		Double hisNum = 1.0;
		Double hisWei = 0.0;
		hisWei = hisFreq / hisNum * totalNum;
		return hisWei;
	}

	/**
	 * 利用比较器给map按照value值排序
	 * 
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
	public List<Map.Entry<String, Double>> calWeight(String s_summary, String s_title, String s_content)// ,Set<String>String category
																															// swSet
	{
		HashMap<String, Double> termWeiMap = new HashMap<String, Double>();
		// System.out.println("I am here 1.");
		HashMap<String, HashMap<String, Double>> termProp = statistics(s_summary, s_title, s_content);// ,swSet
		// System.out.println("I am here 2.");
		Double lengthMax = getLenMax(termProp);
		Double termNum = (double) termProp.size();
		// /////////////////////////2015.4.20用于修正专业词库keyword////////////////////////////////
		// HashSet<String> specificEntitySet = null;
		// if(category.equals("football"))
		// {
		// specificEntitySet = entityStore.get("football");
		// }
		for (Entry<String, HashMap<String, Double>> entry : termProp.entrySet())
		{
			HashMap<String, Double> termMap = entry.getValue();
			Double weight = 0.0;
			Double freqWei = getFreqWeight(termMap.get("totalNum"));
			Double lenWei = getLenthWeight(termMap.get("length"), lengthMax);
			Double speWei = termMap.get("speech");
			Double locWei = getLocationWeight(termMap.get("summaryNum"), termMap.get("titleNum"), termNum);
			// Double hisWei = getHistoryWeight(); //目前尚未开通此功能
			weight = 1.5 * freqWei + 1.2 * speWei + locWei + 0.7 * lenWei;// hisWei
			if(termMap.get("type") == 1.0)
			{
				termWeiMap.put(entry.getKey()+"_kb", weight);
			} 
			else if(termMap.get("type") == 3.0)
			{}
			else if(termMap.get("type") == 5.0)
			{}
			else if(termMap.get("type") == 7.0)
			{
				termWeiMap.put(entry.getKey()+"_kl", weight);
			}
			else if(termMap.get("type") == 11.0)
			{
				termWeiMap.put(entry.getKey()+"_kr", weight);
			}
		}
		List<Map.Entry<String, Double>> termWeiList = mapSort(termWeiMap);

		// List<Map.Entry<String, Double>> list = ke.calWeight(summary, title,
		// content);
		LinkedList<Map.Entry<String, Double>> termList = new LinkedList<Map.Entry<String, Double>>();
		for (int i = 0; i < 10 && i < termWeiList.size(); i++)
		{
			termList.add(termWeiList.get(i));
		}
		for (int j = 0; j < termList.size(); j++)
		{
			for (int k = j + 1; k < termList.size(); k++)
			{
				if (termList.get(j).getKey().length() == termList.get(k).getKey().length())
				{

				}
				else if (termList.get(j).getKey().length() > termList.get(k).getKey().length())
				{
					if (termList.get(j).getKey().split("_")[0].contains(termList.get(k).getKey().split("_")[0]))
					{
						termList.remove(k);
						k = k - 1;
					}
				}
				else
				{
					if (termList.get(k).getKey().split("_")[0].contains(termList.get(j).getKey().split("_")[0]))
					{
						termList.set(j, termList.get(k));
						termList.remove(k);
						k = k - 1;
					}
				}
			}
		}
		Double jumpWeight = 1.0;
		int finalNum = 0;
		List<Map.Entry<String, Double>> resultList = new ArrayList<Map.Entry<String, Double>>();
		for (Entry<String, Double> entry : termList)
		{
			entry.setValue(jumpWeight);
			jumpWeight -= 0.1;
			resultList.add(entry);
			if (finalNum == 5)
			{
				break;
			}
			finalNum++;
		}
		return resultList;
	}
}
