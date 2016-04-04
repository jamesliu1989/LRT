package test.rank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SimiScore {

	public static int K = 10;   //最大文档列表数
    
	//相似性矩阵，按query
	public static List<double[][]> simiScoreList = new ArrayList<double[][]>();
	
	//得分列表，按query
	public static List<double[]> scoreList = new ArrayList<double[]>();
	
	//重得分列表，按query
	public static List<double[]> reScoreList = new ArrayList<double[]>();
		
	/**
	 * 读取相似性得分文件
	 * N
     * S(1,2) S(1,3) S(1,4) ... S(1,N)
     * S(2,3) S(2,4) ... S(2,N)
     * ...
     * S(N-2,N-1) S(N-2,N)
     * S(N-1,N) 
	 * @param simiScoreFile
	 */
	public static void readSimiScore(String simiScoreFile){
		File file = new File(simiScoreFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = reader.readLine()) != null){
				int docs_of_queryi = Integer.parseInt(line);
//System.err.println(line);
				double[][] docSimis = new double[docs_of_queryi][docs_of_queryi];
				//读取每个query对应的所有文档相似矩阵
				for (int i = 0; i < docs_of_queryi-1; i++) {
					if((line = reader.readLine()) != null){
//System.err.println(line);
						String[] iteams = line.split(" ");
						//按上三角向右对齐输入，s[i][j] == s[j][i]
						/**
						 * 0 S(1,2) S(1,3) S(1,4) ... S(1,N)
						 *          S(2,3) S(2,4) ... S(2,N)
						 *                        ...
                         *               S(N-2,N-1) S(N-2,N)
                         *                          S(N-1,N)    
                         *                          0
						 */
						for (int j = iteams.length-1; j >= 0; j--) {
							docSimis[i][i+j+1] = Double.parseDouble(iteams[j]);
						}						
					}
				}
				reader.readLine();   //跳过空行
				simiScoreList.add(docSimis);
				
/*				for (int j = 0; j < docSimis.length; j++) {
					for (int j2 = 0; j2 < docSimis[0].length; j2++) {
						if(docSimis[j][j2] == 0)
							System.err.print("0.000000");
						else
						   System.err.print(docSimis[j][j2]);
						System.err.print(" ");
					}
					System.err.println("");
				}
				break;*/
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 读取得分列表
	 * @param scoreFile
	 * @param startQid
	 * @param endQid
	 */
	public static void readScore(String scoreFile, int startQid, int endQid){
		File file = new File(scoreFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = "";
			for (int i = 0; i < simiScoreList.size() && i < endQid; i++) {				
				int docs_of_queryi = simiScoreList.get(i).length;
				double[] scoreList_i = new double[docs_of_queryi];
				if (i >= startQid - 1) {     //从测试集起始开始
					for (int j = 0; j < docs_of_queryi; j++) {
						if ((line = reader.readLine()) != null) {
							scoreList_i[j] = Double.parseDouble(line);
						}
					}
				}
//System.out.println(scoreList_i.length);
				scoreList.add(scoreList_i);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 利用文档相似性对文档进行打分
	 * @param startQid
	 * @param endQid
	 */
	public static void reScore(int startQid, int endQid){
		for (int i = 0; i < scoreList.size() && i < endQid; i++) {  //i代表每个查询
			double[] score = scoreList.get(i);
			double[] reScore = new double[score.length];
			if (i >= startQid - 1) {     //从测试集起始开始
				// recore[j] = score[j] + score[k!=j]*simi[k][j]/(n-1) 所有k !=
				// j对j做打分
				for (int j1 = 0; j1 < score.length; j1++) {
					for (int j2 = 0; j2 < score.length; j2++) {
						if (j2 != j1) {
							if (j2 < j1) { // simiScore矩阵为上三角向右对称矩阵
								reScore[j1] += score[j2] * simiScoreList.get(i)[j2][j1];
							} else {
								reScore[j1] += score[j2] * simiScoreList.get(i)[j1][j2];
							}
						}
					}
					reScore[j1] = score[j1] + reScore[j1] / (score.length - 1);
// System.err.println(reScore[j1]);
				}
			}
			reScoreList.add(reScore);
		}
	}
	
	/**
	 * 输出加权得分列表
	 * @param outputFile
	 * @param startQid
	 * @param endQid
	 */
	public static void ouputScore(String outputFile,int startQid, int endQid) {
		File file = new File(outputFile);
		BufferedWriter writer = null;
		DecimalFormat df = new DecimalFormat("0.00000");
		int total = 0;
		try {
			writer = new BufferedWriter(new FileWriter(file));			
			for (int i = startQid-1; i < reScoreList.size() && i < endQid; i++) {
				double[] score = reScoreList.get(i);
System.err.println(score.length);
				total += score.length;
				for (int j = 0; j < score.length; j++) {
					//writer.write(df.format(score[j])+"     "+df.format(scoreList.get(i)[j])+"\n");
					writer.write(df.format(score[j])+"\n");
				}
System.err.println("*************************************"+total);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void main(String[] args) {
		readSimiScore("E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\ohsumed_simi\\ohsumed_simi.txt");
		
		readScore("E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\ListNet.testscore\\ListNet.OHSUMED.Fold1.test.txt", 85, 106);
		
		reScore(85, 106);
		
		ouputScore("E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\ListNet.testscore\\ListNet.OHSUMED.Fold1.test2.txt", 85, 106);
	}
	
	

}
