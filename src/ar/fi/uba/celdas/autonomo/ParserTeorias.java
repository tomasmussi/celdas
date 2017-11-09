package ar.fi.uba.celdas.autonomo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ParserTeorias {
	
	public static List<Teoria> leerTeorias() {
		List<Teoria> teorias = new ArrayList<Teoria>();
		try {
			String teoriasFile = "/home/tomas/Escritorio/teorias.json";
			InputStream fstream = new FileInputStream(new File(teoriasFile));		

			int content;
			StringBuffer sb = new StringBuffer();
			try {
				while ((content = fstream.read()) != -1) {
					// convert to char and display it
					sb.append((char) content);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			JSONObject js = new JSONObject(sb.toString());
			JSONArray array = js.getJSONArray("teorias");
			Iterator it = array.iterator();
			while (it.hasNext()) {
				JSONObject jsonTeoria = (JSONObject) it.next();
				Teoria t = new Teoria(jsonTeoria);
				teorias.add(t);				
			}
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return teorias;
	}
	
	public static void persistirTeorias(List<Teoria> teorias) {
		try {
			JSONObject js = new JSONObject();
			js.put("teorias", teorias);
		
			String teoriasFile = "/home/tomas/Escritorio/teorias.json";
			File file;
			file = new File(teoriasFile);
			OutputStream output = new FileOutputStream(file);
			PrintWriter w = new PrintWriter(output);
			w.write(js.toString());
			w.close();
		} catch (Exception e) {
			System.out.println("error...");
			e.printStackTrace();
		}
	}

}
