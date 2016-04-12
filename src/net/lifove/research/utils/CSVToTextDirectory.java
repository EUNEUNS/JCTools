package net.lifove.research.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.csvreader.CsvReader;

public class CSVToTextDirectory {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CSVToTextDirectory runner = new CSVToTextDirectory();
		//runner.run(args);
		args[0] = "data/pitsAll.csv";
		runner.run(args);
		/*args[0] = "data/pitsC.csv";
		runner.run(args);
		args[0] = "data/pitsD.csv";
		runner.run(args);
		args[0] = "data/pitsE.csv";
		runner.run(args);
		args[0] = "data/pitsF.csv";
		runner.run(args);*/
		
	}

	void run(String[] args){
try {
			
			CsvReader products = new CsvReader(args[0]);
		
			products.readHeaders();

			int i=0;
			while (products.readRecord())
			{
				i++;
				
				if(products.get("Severity").trim()==""){
					System.out.println("no severity info...ignore! " + args[0]);
					continue;
				}
				
				int severity = Integer.parseInt(products.get("Severity"));
				String description = products.get("Description");
				
				String label = "clean";
				if (severity >=3)
					label="buggy";
				
				String fileName = args[0].substring(0, args[0].indexOf('.')) + File.separator + label + File.separator + String.format("%04d", i) + ".txt";
				
				try {
					File file= new File(fileName);
					file.getParentFile().mkdirs();
			
					FileOutputStream fos = new FileOutputStream(file);
					DataOutputStream dos=new DataOutputStream(fos);
					dos.write(description.getBytes());
					dos.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
	
			products.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
