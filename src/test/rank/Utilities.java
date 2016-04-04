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

public class Utilities {
	
	public static List<Double> score = new ArrayList<Double>();
	/**
	 * 读取得分列表
	 * @param scoreFile
	 * @param startQid
	 * @param endQid
	 */
	public static void readScore(String scoreFile){
		File file = new File(scoreFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = reader.readLine()) != null) {
					score.add(Double.parseDouble(line.split("	")[2]));
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
	 * 输出加权得分列表
	 * @param outputFile
	 * @param startQid
	 * @param endQid
	 */
	public static void ouputScore(String outputFile) {
		File file = new File(outputFile);
		BufferedWriter writer = null;
		DecimalFormat df = new DecimalFormat("0.00000");
		try {
			writer = new BufferedWriter(new FileWriter(file));			
			for (int i = 0; i < score.size(); i++) {
					writer.write(df.format(score.get(i))+"\n");
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
		
		readScore("E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold1\\scorefile.txt");
		
		ouputScore("E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold1\\scorefile2.txt");
	}
}
