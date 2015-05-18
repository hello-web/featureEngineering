package xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.LoadConfig;

public class ItemEditDistance
{
		
		private static int Minimum(int a, int b, int c)
		{
			int mi;
			mi = a;
			if (b < mi)
			{
				mi = b;
			}
			if (c < mi)
			{
				mi = c;
			}
			return mi;
		}

		public static int getEditDistance(String[] s, String[] t)
		{
			int d[][]; // matrix
			int n; // 原串s的长度
			int m; // 对比串t的长度
			int i; // iterates through s
			int j; // iterates through t
			String s_i; // ith character of s
			String t_j; // jth character of t
			int cost; // cost
			// Step 1
			//获取字符串长度，构建路径矩阵
			n = s.length;
//			System.out.println("n is "+n);
			m = t.length;
//			System.out.println("m is "+m);
			if (n == 0)
			{
				return m;
			}
			if (m == 0)
			{
				return n;
			}
			d = new int[n][m];

			// Step 2
			//初始化矩阵，第一行第一列

			for (i = 0; i < n; i++)
			{
				d[i][0] = i;
//				System.out.println("d["+i+"][0] is "+d[i][0]);
			}

			for (j = 0; j < m; j++)
			{
				d[0][j] = j;
//				System.out.println("d[0]["+j+"] is "+d[0][j]);
			}

			// Step 3

			for (i = 1; i < n; i++)
			{
				s_i = s[i-1];
//				System.out.println("s_i is "+s_i);
				// Step 4
				for (j = 1; j < m; j++)
				{
					t_j = t[j-1];
//					System.out.println("t_j is "+t_j);
					// Step 5
					if (s_i.equals(t_j))
					{
						cost = 0;
					}
					else
					{
						cost = 1;
					}
					// Step 6
					d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
				}
			}
			// Step 7
			return d[n-1][m-1];
		}
		public static double similarity(String[] str1, String[] str2)
		{
			if(str1 == null || str2 == null)
			{
				return 0.0;
			}
			int min = getEditDistance(str1, str2);
			double similarity = 1 - (double) min / ((str1.length + str2.length) / 2);
			return similarity;
		}
		public static void main(String[] args)
		{
			String[] str1 = {"悦", "享", "自然", "之", "礼", "NaturesGift", "纽", "格", "芙", "震撼", "登陆", "中国"};
			String[] str2 = null;
			double x = ItemEditDistance.similarity(str1, str2);
			System.out.println(x);
		}
}

