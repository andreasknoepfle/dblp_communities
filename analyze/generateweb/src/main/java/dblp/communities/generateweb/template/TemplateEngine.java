package dblp.communities.generateweb.template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateEngine {
	private static final String Extension = ".html";
	public static final String NODETEMPLATE = "node.ftl";
	public static final String INDEXTEMPLATE = "index.ftl";
	public static final String FRAMESETTEMPLATE ="frameset.ftl";
	public static final String DOUBLETOPLIST = "doubletop.ftl";
	public static final String YEARTEMPLATE = "year.ftl";
	Template temp;
	File out;
	
	public TemplateEngine(File outdir,File templatedir,String templatename) throws IOException {
		Configuration cfg = new Configuration();
		cfg.setDirectoryForTemplateLoading(templatedir);
		cfg.setObjectWrapper(new DefaultObjectWrapper());  
		 temp = cfg.getTemplate(templatename);
		 this.out=outdir;
	}

	public void generateTemplate(Map<String,Object> map, String filename) {
		
		try {
			File outfile=new File(out,filename+Extension);
		outfile.createNewFile();
		
		FileOutputStream fos=new FileOutputStream(outfile);
		Writer out = new OutputStreamWriter(fos,"UTF-8");
		temp.process(map, out);
		out.flush(); 
		out.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
