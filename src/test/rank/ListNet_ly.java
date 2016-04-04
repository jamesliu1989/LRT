package test.rank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListNet_ly {

	private static int DIMENSION = 45;                                //特征维度
	
	private static double[] weight = new double[DIMENSION];           //特征权重
	
	private static int nIteration = 100;                             //迭代次数
	
	private static double learningRate = 0.0025;                      //学习步长
		
	private static List<Docs_of_query> samples = new ArrayList<Docs_of_query>();    //样本数据
	
	//相似度打分部分参数
	private static double u = 0.03;                                  //相关度打分权重
	
	//相似性矩阵，按query
	public static List<double[][]> simiScoreList = new ArrayList<double[][]>();
	
	//查询q的所以样本数据
	static class Docs_of_query{	
		public int qid;
		public List<Doc> docs;
				
		public Docs_of_query(int qid) {
			this.qid = qid;
			this.docs = new ArrayList<Doc>();
		}
	}
	//一篇文档
	static class Doc{
		public int label;
		public double[] features;
		
		public Doc(int label) {
			this.label = label;
			this.features = new double[DIMENSION];;
		}
		
		
	}
	
	/**
	 * 读取训练样本
	 * @param inputFile
	 */
	public static void readTxtFile(String inputFile){
		File file = new File(inputFile);  
        if(file.isFile() && file.exists()) { //判断文件是否存在  
        	BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
	            String line = null;  
	            int last_qid = -1;
	            Docs_of_query docs_of_query = null;
	            while((line = reader.readLine()) != null){
	            	String[] subArray = line.split(" ");
	            	int label = Integer.parseInt(subArray[0]);
	            	int qid = Integer.parseInt(subArray[1].split(":")[1]);
	            	if(qid != last_qid){
	            		docs_of_query = new Docs_of_query(qid);   //如果是新的qid则新new一个查询样本集Docs_of_query
	            		samples.add(docs_of_query);
	            	}
	            	Doc doc = new Doc(label);
	            	for (int i = 0; i < DIMENSION; i++) {
	            		doc.features[i] = Double.parseDouble(subArray[i+2].split(":")[1]);	            		
					}
	            	docs_of_query.docs.add(doc);
	            	last_qid = qid;
	            }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }              
	}
	
	/**
	 * 学习过程
	 */
	public static void learning(){
		
		
		
        //初始化权重
        for(int i = 0; i < weight.length;i++) {
            weight[i] = (double) 0.0; //权重初值  
        }
        System.err.println("training...");     
        //迭代nIteration次
        for (int iter = 1; iter <= nIteration; iter++) {
        	System.out.println("---迭代次数:"+iter);
        	int query_num = samples.size();   //query个数

        	for (int i = 0; i < query_num; i++) {
        		Docs_of_query docs_of_query = samples.get(i);    //该query返回的所有文档
        		int doc_num_of_i = docs_of_query.docs.size();
       		 
        		double score[] = new double[doc_num_of_i];

        		double[] labelExp = new double[doc_num_of_i];
        		double[] scoreExp = new double[doc_num_of_i];
        		double sumLabelExp = 0.0;
        		double sumScoreExp = 0.0;
        		
        		for (int j = 0; j < doc_num_of_i; j++) {
        			score[j] = 0.0;      //初始化  
            		/**
            		 * step1....计算query_i的文档列表得分Z（fw）
            		 */
					for (int j2 = 0; j2 < DIMENSION; j2++) {
						score[j] += weight[j2] * docs_of_query.docs.get(j).features[j2];
					}
					
					labelExp[j] = Math.exp(docs_of_query.docs.get(j).label);
					sumLabelExp += labelExp[j];
					
					scoreExp[j] = Math.exp(score[j]);
					sumScoreExp += scoreExp[j];
				}
       		
        		for (int k = 0; k < DIMENSION; k++) {       			
        			double p1 = 0.0;
        			double p2 = 0.0;
            		/**
            		 * step2.....计算-Σp1*logP2
            		 */
            		for (int j = 0; j < doc_num_of_i; j++) {
            			double feature = docs_of_query.docs.get(j).features[k];
        				p1 -= labelExp[j] * feature;
        				p2 += scoreExp[j] * feature; 
    				}
            		double delta_w = p1 / sumLabelExp + p2 / sumScoreExp;
            		/**
            		 * step3......更新权重
            		 */ 
            		weight[k] = weight[k] - learningRate * delta_w;
            		//System.err.println("weight"+ k +": "+weight[k]);
				}
   		
			}        
        	System.out.println("finish training " + (iter) + "/" + nIteration);
		}
	}
	
	/**
	 * 预测得分
	 * @param inputFile
	 * @param outputFile
	 */
	public static void prediction(String inputFile, String outputFile){
		File file = new File(inputFile);  
		List<Double> scoreList = new ArrayList<Double>();
		
        if(file.isFile() && file.exists()) { //判断文件是否存在  
        	BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
	            String line = null;  
	            while((line = reader.readLine()) != null){
	            	String[] subArray = line.split(" ");
	            	double score = 0.0;
	            	for (int j = 0; j < DIMENSION; j++) {
						score += weight[j] * Double.parseDouble(subArray[j+2].split(":")[1]);
					}
	            	scoreList.add(score);
	            }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				try {
				  if(reader != null)
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        } 
        
        //保存预测得分列表       
        BufferedWriter writer = null;     
        try {
			writer = new BufferedWriter(new FileWriter(new File(outputFile)));
			for (int i = 0; i < scoreList.size(); i++) {
				writer.write(scoreList.get(i)+"\r\n");				
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(writer != null)
				   writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 保存训练模型
	 * @param modelFile
	 */
	public static void saveModel(String modelFile){           
        BufferedWriter writer = null;     
        try {
			writer = new BufferedWriter(new FileWriter(new File(modelFile)));
			writer.write("## ListNet");  
			writer.write("\r\n");  
			writer.write("## Epochs = "+nIteration);  
			writer.write("\r\n");  
			writer.write("## No. of features = 45");  
			writer.write("\r\n");  
			writer.write("1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45");  
			writer.write("\r\n");  
			writer.write("0");  
			writer.write("\r\n"); 
			for (int i = 0; i < weight.length; i++) {
				writer.write("0 "+i+" "+weight[i]+"\r\n"); 			
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(writer != null)
				   writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
		
	public static void main(String args[]){
		
		int fold = 1;
		String train = "E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold"+ fold +"\\train.txt";
		String modelFile = "E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold"+ fold +"\\model_ly.txt";
		String test = "E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold"+ fold +"\\test.txt";
		String score = "E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold"+ fold +"\\score_ly_100.txt";
		
		readTxtFile(train);
		
		learning();
		
		//saveModel(modelFile);
		
		prediction(test, score);
		
	}
}
