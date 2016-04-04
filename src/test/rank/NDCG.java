package test.rank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NDCG {

	public static int K = 10;   //最大文档列表数
	
	static class Pair{
		int label;
		double score;
		
		public Pair(int label, double score) {
			this.label = label;
			this.score = score;
		}

		@Override
		public String toString(){
			return label+":"+score;
			
		}
	}
	
	public void readFile(){
		File file = new File("");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = reader.readLine()) != null){
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		int query_num = 0;
		Map<Integer, List<Pair>> queryMap = new LinkedHashMap<Integer, List<Pair>>();
		
		//测试集输入
		File testset = new File("E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold1\\test.txt");
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(testset));
			String line = "";
			List<Pair> pairList = null;
			int last_qid = 0;
			while((line = reader.readLine()) != null){
				String[] records = line.split(" ");
				int label = Integer.parseInt(records[0]);
				int qid = Integer.parseInt(records[1].split(":")[1]);
				if(qid != last_qid){
					query_num++;
					pairList = new ArrayList<Pair>();
					queryMap.put(qid, pairList);
				}
				Pair pair = new Pair(label, 0);
				pairList.add(pair);
				last_qid = qid;
			}
			reader.close();
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
		//预测排序输入
		File predictions = new File("E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold1\\score.txt");
		try {
			reader = new BufferedReader(new FileReader(predictions));
			
			for(Entry<Integer, List<Pair>> entrys : queryMap.entrySet()){
				List<Pair> pairList = entrys.getValue();
				for(Pair p : pairList){
					p.score = Double.parseDouble(reader.readLine().split("	")[2]);   //注意预测得分数据格式
				}
				Collections.sort(pairList, new Comparator<Pair>() {   //对数据按预测得分排序
					//按score降序排列
					@Override
					public int compare(Pair p1, Pair p2) {
						if(p1.score > p2.score){
							return -1;
						}else if(p1.score < p2.score){
							return 1;
						}else {
							return 0;
						}
					}
				});   
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		

		double NDCG = 0;
		for(Entry<Integer, List<Pair>> entrys : queryMap.entrySet()){
			
			//System.err.println(entrys.getKey()+":"+entrys.getValue());			
			List<Pair> pairList = entrys.getValue();
			
			//计算 NDCG_i
			double NDCG_i = 0;			
			for(int i = 1; i <= Math.min(pairList.size(), K); i++){
				int label = pairList.get(i-1).label;
				NDCG_i += (Math.pow(2, label)-1)*Math.log(2) / Math.log(i+1);
				System.err.print(label+",");
			}
			System.err.println("");
			
			//计算ideal NDCG
			double INDCD = 0;
			Collections.sort(pairList, new Comparator<Pair>() {   //对数据按标记label排序
				//按label降序排列
				@Override
				public int compare(Pair p1, Pair p2) {
					if(p1.label > p2.label){
						return -1;
					}else if(p1.label < p2.label){
						return 1;
					}else {
						return 0;
					}
				}
			});
			for(int i = 1; i <= Math.min(pairList.size(), K); i++){
				int label = pairList.get(i-1).label;
				INDCD += (Math.pow(2, label)-1)*Math.log(2) / Math.log(i+1);
				System.err.print(label+",");
			}
			NDCG += NDCG_i / INDCD;
			System.err.println("");
		}
		NDCG = NDCG / query_num;
		
		DecimalFormat df=new DecimalFormat("0.0000");
		//System.err.println("NDCG: "+df.format(NDCG));
		System.err.println("NDCG: "+NDCG);
		
		
	}
	
	

}
