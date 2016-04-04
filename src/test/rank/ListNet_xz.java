package test.rank;

	import java.io.BufferedReader;  
	import java.io.File;  
	import java.io.FileInputStream;  
	import java.io.FileWriter;  
	import java.io.InputStreamReader;  
	  
	public class ListNet_xz {  
	      
	    //文件总行数(标记数)  
	    private static int sumLabel;                     
	    //特征值 46个 (标号1-46)  
	    private static double feature[][] = new double[100000][48];                  
	    //特征值权重 46个 (标号1-46)  
	    private static double weight [] = new double[48];  
	    //相关度 其值有0-2三个级别 从1开始记录  
	    private static int label [] = new int[1000000];  
	    //查询id 从1开始记录  
	    private static int qid [] = new int[1000000];  
	    //每个Qid的doc数量  
	    private static int doc_ofQid[] = new int[100000];   
	  
	    private static int ITER_NUM=5;     //迭代次数  
	    private static int weidu=45;        //特征数  
	    private static int qid_Num=0;       //Qid数量  
	    private static int tempQid=-1;      //临时Qid数  
	    private static int tempDoc=0;       //临时doc数  
	      
	    /**   
	     * 函数功能 读取文件 
	     * 参数 String filePath 文件路径 
	     */  
	    public static void ReadTxtFile(String filePath) {  
	        try {  
	            String encoding="GBK";  
	            File file=new File(filePath);  
	            if(file.isFile() && file.exists()) { //判断文件是否存在  
	                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);   
	                BufferedReader bufferedReader = new BufferedReader(read);  
	                String lineTxt = null;  
	                sumLabel =1; //初始化从1记录  
	                //按行读取数据并分解数据  
	                while((lineTxt = bufferedReader.readLine()) != null) {  
	                    String str = null;  
	                    int lengthLine = lineTxt.length();  
	                    //获取数据 字符串空格分隔  
	                    String arrays[] = lineTxt.split(" ");  
	                    for(int i=0; i<arrays.length; i++) {  
	                        //获取每行样本的Label值  
	                        if(i==0) {  
	                            label[sumLabel] = Integer.parseInt(arrays[0]);  
	                        }   
	                        else if(i>=weidu+2){ //读取至#跳出 0-label 1-qid 2:47-特征  
	                            continue;  
	                        }  
	                        else {  
	                            String subArrays[] = arrays[i].split(":"); //特征:特征值  
	                            if(i==1) { //获取qid        
	                                //判断是否是新的Qid  
	                                if(tempQid != Integer.parseInt(subArrays[1])) {   
	                                    if(tempQid != -1){ //不是第一次出现新Qid  
	                                        //赋值上一个为qid_Num对应的tempDoc个文档  
	                                        doc_ofQid[qid_Num]=tempDoc;      
	                                        tempDoc=0;  
	                                    }  
	                                    //当tempQid不等于当前qid时下标加1   
	                                    //相等则直接跳至Doc加1直到不等  
	                                    qid_Num++;  
	                                    tempQid=Integer.parseInt(subArrays[1]);                       
	                                }  
	                                tempDoc++; //新的文档   
	                                qid[sumLabel] = Integer.parseInt(subArrays[1]);  
	                            }   
	                            else { //获取46维特征值  
	                                int number = Integer.parseInt(subArrays[0]); //判断特征  
	                                double value = Double.parseDouble(subArrays[1]);  
	                                feature[sumLabel][number] = value; //number数组标号:1-46  
	                            }  
	                        }  
	                    }  
	                    sumLabel++;  
	                }  
	                doc_ofQid[qid_Num]=tempDoc;  
	                read.close();  
	            } else {  
	                System.out.println("找不到指定的文件\n");  
	            }  
	        } catch (Exception e) {  
	            System.out.println("读取文件内容出错");  
	            e.printStackTrace();  
	        }  
	    }  
	  
	    /** 
	     * 学习排序 
	     * 训练模型得到46维权重 
	     */  
	    public static void LearningToRank() {  
	          
	        //变量  
	        double index [] = new double[1000000];  
	        double tao [] = new double[1000000];  
	        double yita=0.00003;  
	        //初始化  
	        for(int i=0;i<weidu+2;i++) { //从1到136为权重，0和137无用  
	            weight[i] = (double) 1.0; //权重初值  
	        }  
	        System.out.println("training...");                
	        //计算权重 学习算法  
	        for(int iter = 0; iter<ITER_NUM; iter++) //迭代ITER_NUM次  
	        {   
	            System.out.println("---迭代次数:"+iter);  
	            int now_doc=0; //全局文档索引 ，当前查询的文档起始序号
	            for(int i=1; i<=qid_Num; i++) //总样qid数  相当于两层循环T和m   
	            {   
	                double delta_w[] = new double[weidu+2]; //46个梯度组成的向量  
	                int doc_of_i=doc_ofQid[i]; //该Qid的文档数  
	                //得分f(w),一个QID有多个文档，一个文档为一个分,所以一个i对应一个分数数组  
	                double fw[] = new double[doc_of_i+2];  
	                  
	                /* 第一步 算得分数组fw fin */  
	                for(int k=1;k<=doc_of_i;k++) { //初始化  
	                    fw[k]=0.0;  
	                }  
	                for(int k=1;k<=doc_of_i;k++) { //每个文档的得分  
	                    for(int p=1;p<=weidu;p++) {  
	                        fw[k] += weight[p]*feature[now_doc+k][p]; //算出这个文档的分数  
	                    }  
	                }  
	                  
	                /* 
	                 * 第二步  算梯度delta_w向量 
	                 * a=Σp*x,a是向量   
	                 * b=Σexpf(x),b是数字 
	                 * c=expf(x)*x,c是向量 
	                 * 最终结果delta_w是向量 
	                 */  
	                double[] a=new double[weidu+2],c=new double[weidu+2];  
	                for(int k=0;k<weidu+2;k++){a[k]=0.0;} //初始化  
	                for(int k=0;k<weidu+2;k++){c[k]=0.0;} //初始化  
	                double b=0.0;  
	                //算a：----  
	                for(int k=1; k<=doc_of_i; k++) {  
	                    double p=1.0; //先不topK  
	                    double[] temp=new double[48];  
	                    for(int q=1;q<=weidu;q++) {  
	                        //算P: ----第q个向量排XX的概率是多少  
	                        //分母：  
	                        double fenmu=0.0;  
	                        for(int m=1;m<=doc_of_i;m++) {  
	                            fenmu=fenmu+Math.exp(fw[m]); //所有文档得分  
	                        }  
	                        //top-1  exp(s1) / exp(s1)+exp(s2)+..+exp(sn)  
	                        for(int m=1;m<=doc_of_i;m++) {  
	                            p=p*(Math.exp(fw[m])/fenmu);  
	                        }  
	                        //算积  
	                        temp[q]=temp[q]+p*feature[now_doc+k][q];  
	                    }  
	                    for(int q=1; q<=weidu; q++){           
	                        a[q]=a[q]+temp[q];  
	                    }     
	                } //End a  
	                //算b：---- fin.  
	                for(int k=1; k<=doc_of_i; k++){  
	                    b=b+Math.exp(fw[k]);  
	                }  
	                //算c：----  
	                for(int k=1; k<=doc_of_i; k++){  
	                    double[] temp=new double[weidu+2];  
	                    for(int q=1; q<=weidu; q++){           
	                        temp[q]=temp[q]+Math.exp(fw[k])*feature[now_doc+k][q];  
	                    }  
	                    for(int q=1; q<=weidu; q++){           
	                        c[q]=c[q]+temp[q];  
	                    }     
	                }  
	                //算梯度：delta_x=-a+1/b*c  
	                for(int q=1; q<=weidu; q++){  
	                    delta_w[q]= (-1)*a[q] + ((1.0/b)*c[q]);  
	                }  
	                //**********  
	                  
	                /* 第三步 更新权重 fin. */  
	                for(int k=1; k<=weidu; k++){  
	                    weight[k]=weight[k]-yita*delta_w[k];  
	                }  
	                now_doc=now_doc+doc_of_i; //更新当前文档索引  
	            }  
	        } //End 迭代次数  
	          
	        //输出权重  
	        for(int i=1;i<=weidu;i++) //从1到136为权重，0和137无用  
	        {  
	            System.out.println(i+"wei:"+weight[i]);  
	        }  
	    }  
	      
	    /** 
	     * 输出权重到文件fileModel 
	     * @param fileModel 
	     */  
	    public static void WriteFileModel(String fileModel) {  
	        //输出权重到文件  
	        try {  
	            System.out.println("write start.总行数："+sumLabel);  
	            FileWriter fileWriter = new FileWriter(fileModel);  
	            //写数据  
	            fileWriter.write("## ListNet");  
	            fileWriter.write("\r\n");  
	            fileWriter.write("## Epochs = "+ITER_NUM);  
	            fileWriter.write("\r\n");  
	            fileWriter.write("## No. of features = 46");  
	            fileWriter.write("\r\n");  
	            fileWriter.write("1 2 3 4 5 6 7 8 9 10 ...  39 40 41 42 43 44 45 46");  
	            fileWriter.write("\r\n");  
	            fileWriter.write("0");  
	            fileWriter.write("\r\n");  
	            for(int k=0; k<weidu; k++){  
	                fileWriter.write("0 "+k+" "+weight[k+1]);  
	                fileWriter.write("\r\n");  
	            }  
	            fileWriter.close();  
	            System.out.println("write fin.");  
	        } catch(Exception e) {  
	            System.out.println("写文件内容出错");  
	            e.printStackTrace();  
	        }  
	    }  
	      
	    /** 
	     * 预测排序 
	     * 正规应对test.txt文件进行打分排序 
	     * 但我们是在Hadoop实现该打分排序步骤 此函数仅测试train.txt打分 
	     */  
	    public static void PredictRank(String fileScore) {  
	        //输出得分  
	        try {  
	            System.out.println("write start.总行数："+sumLabel);  
	            String encoding = "GBK";  
	            FileWriter fileWriter = new FileWriter(fileScore);  
	            //写数据  
	            for(int k=1; k<sumLabel; k++){  
	                double score=0.0;  
	                for(int j=1;j<=weidu;j++){  
	                    score=score+weight[j]*feature[k][j];  
	                }  
	                fileWriter.write("qid:"+qid[k]+" score:"+score+" label:"+label[k]);  
	                fileWriter.write("\r\n");  
	            }     
	            fileWriter.close();  
	            System.out.println("write fin.");     
	        } catch(Exception e) {  
	            System.out.println("写文件内容出错");  
	            e.printStackTrace();  
	        }  
	    }  
	      
	    /** 
	     * 主函数 
	     */  
	    public static void main(String args[]) {  
	        String fileInput = "E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold1\\train.txt";       //训练  
	        String fileModel = "E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold1\\model_weight.txt";       //输出权重模型  
	        String fileScore = "E:\\桌面\\毕业论文\\实验\\Datasets\\LETOR\\3.0\\OHSUMED\\QueryLevelNorm\\Fold1\\score_listNet.txt";      //输出得分  
	        //第1步 读取文件并解析数据  
	        System.out.println("read...");  
	        ReadTxtFile(fileInput);  
	        System.out.println("read and write well.");  
	        //第2步 排序计算  
	        LearningToRank();  
	        //第3步 输出模型  
	        WriteFileModel(fileModel);  
	        //第4步 打分预测排序  
	        PredictRank(fileScore);  
	      }  
	      
	    /* 
	     * End 
	     */  
	      
	}
