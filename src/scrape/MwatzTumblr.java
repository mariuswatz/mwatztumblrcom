/*
 * MwatzTumblr.java - Marius Watz, January 2013
 * https://github.com/mariuswatz/mwatztumblrcom
 * 
 * Code used to reformat the original HTML scrape of mwatz.tumblr.com
 * for publishing as a proper archive. For purposes of convenience I am
 * using Processing as application framework, although I barely use any
 * of its core functions. All parsing and generating of HTML is done 
 * using JSoup (http://jsoup.org/)  
 * 
 * This code written as an ad hoc hack. It's messy and full of redunancies,
 * but I'm providing it for reference anyway. A close reading might provide 
 * a few insights into how JSoup (which is a very a powerful tool) can scrape 
 * HTML and extract desired sections, from which new documents can be created
 * based on a custom HTML template.
 * 
 * I used a local web server to provide access to the original scraped HTML
 * as produced by the Firefox Scrapbook extension (hence the use of
 * URLBASE="http://127.0.0.1/tumblr/".) All generated files are initialized
 * using the template 'html/templ/templ-page.html" and content is injected using
 * org.jsoup.nodes.Document.append() etc. A ZIP of the original scrape can be
 * found at the GitHub link above. 
 * 
 * Running this code as-is will be difficult without replicating my local setup,
 * including the local server URL. I recommend using it only as a reference,
 * If I had a clearer strategy from the start I would have written this 
 * very differently. 
 *  
 * Licensed under CC license
 * (CC BY-NC-ND 3.0) Attribution-NonCommercial-NoDerivs 3.0 Unported 
 * http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

package scrape;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;

import org.jsoup.*;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import processing.core.PApplet;
import processing.core.PImage;

public class MwatzTumblr extends PApplet {
	private static final String URLBASE="http://127.0.0.1/tumblr/";
	ArrayList<Page> pages;
	ArrayList<String> files;
	TagMap tagmap;
	Document baseDoc;
	String aboutHtml,aboutHtmlShort;
	boolean doAbout,doAnalytics=true;
	
	
	public void setup() {
		size(600,600);
//		appRenderer=JAVA2D;
		tagmap=new TagMap();
		
		baseDoc=getDoc("html/templ/about.html");
		aboutHtml=baseDoc.select("#about").outerHtml();
		aboutHtmlShort=baseDoc.select("#aboutShort").outerHtml();

		baseDoc=getDoc("html/templ/templ-page.html");
		findFiles();
			
		tagmap.finish();
		writeIndex();
		
		exit();
	}
	
	private void writeIndex() {
		Document newpage=getBaseTemplate("Archive index");
		String html="";
				
		newpage.select("#content h3").after(aboutHtmlShort);
		String ulText="";
		int lastMonth[]=pages.get(0).dateVal;
		
		for(Page pg : pages) {
			if(pg.dateVal[1]!=lastMonth[1]) {
				html+=div("pageMonth",months[lastMonth[1]]+", "+lastMonth[2]);
				html+=ul("pageIndex",ulText);
				ulText="";
			}			
			ulText+="<li>"+href(pg.filename, pg.title)+
//					" "+span("indexDate",pg.date)+ 
					"</li>\n";
			lastMonth=pg.dateVal;
		}
		
		html+=div("pageMonth",months[lastMonth[1]]+", "+lastMonth[2]);
		html+=ul("pageIndex",ulText);
		
		newpage.select("#contentMain").append(html);
		newpage.select("#content").prepend(aboutHtmlShort);
		analyticsScript(newpage);
		saveHTML("index.html",newpage.html());

		
		html="";
		ulText="";
		for(Page pg : pages) {
			ulText+="<li>"+href(pg.filename, pg.title)+
//				" "+span("indexDate",pg.date)+ 
				"</li>\n";
		}
		html=ul("pageIndex",ulText);
		newpage=getBaseTemplate("Index - raw");
		newpage.select("#contentMain").append(html);
		analyticsScript(newpage);
		saveHTML("index-raw.html",newpage.html());

		
		newpage=getBaseTemplate("About");
		newpage.select("#content").prepend(aboutHtml);
		analyticsScript(newpage);
		saveHTML("index-about.html",newpage.html());
		
		Collections.sort(pages, new Comparator<Page>() {
	    public int compare(Page a, Page b) {
	        return a.filename.compareTo(b.filename);
	    }
		});
		
		html="";
		newpage=getBaseTemplate("Index - alphabetical");
		for(Page pg : pages) {
			html+="<li>"+href(pg.filename,pg.title)+"</li>\n";
		}
		newpage.select("#content").prepend(ul("pageIndex",html));
		analyticsScript(newpage);
		saveHTML("index-alpha.html",newpage.html());	
	}

	private void findFiles() {
		Document doc;
		
		try {
			pages=new ArrayList<MwatzTumblr.Page>();
			doc=Jsoup.connect(URLBASE+"archive-alledit.html").get();
			Elements pg = doc.select("a.brick"); // a with href
			println(pg.size()+" a.brick");
			files=new ArrayList<String>();
			ArrayList<String> dat=new ArrayList<String>();
			
//			int removeCnt=pg.size()-20;
//			for(int i=0; i<removeCnt; i++) pg.remove(20);
			
			int cnt=0;
			Page thePage=null,prev=null;
			for(Element el : pg)  {
				String s=el.html();
				String url=el.attr("href");
				if(url.startsWith("http")) url=url.substring(url.lastIndexOf('/')+1);
				if(!url.endsWith("html")) url=url+".html";
				files.add(url);
				
				thePage=new Page(url);
				if(prev!=null) {
					thePage.prev=prev;
					prev.next=thePage;
				}
				prev=thePage;
				
				pages.add(thePage);
				println(files.size()+" "+url);
			}
			
			
			for(Page p : pages)  p.buildPage();

			
			String str[]=(String [])dat.toArray(new String [dat.size ()]);
			saveStrings("web-archive.dat", str);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fetchImage(String url) {
//Open a URL Stream
        try {
        	new File("html/images/").mkdirs();
        	
					String filename=url.substring(url.lastIndexOf('/')+1);
					Response resultImageResponse = Jsoup.connect(url).ignoreContentType(true).execute();
					File imgfile=new java.io.File("html/images/" + filename);
					
					if(!imgfile.exists()) {
						// output here
						FileOutputStream out = new FileOutputStream(imgfile);
								
						out.write(resultImageResponse.bodyAsBytes());           // resultImageResponse.body() is where the image's contents are.
						out.close();					
						
//						println("Downloaded: "+filename+" "+new File(filename).length());
					}
//					else println("Exists: "+filename);
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
	}

	public void draw() {
		
	}

	private Document getDoc(String url) {
		Document doc=null;
		try {
			doc=Jsoup.parse(new File(url),"ISO-8859-1","");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doc;
	}

	private Document getBaseTemplate(String title) {
		Document doc=baseDoc.clone();
		doc.select("title").remove();
		doc.select("head").append("<title>Marius Watz | "+title+" | mwatz.tumblr.com archive</title>");

		return doc;
	}

	class Page {
		Document page,newpage;
		Page prev,next;
		String html,title,url,filename,date;
		ArrayList<String> imgs,tags;
		private int[] dateVal;
		
		
		Page(String url) throws Exception {			
			this.url=url;
			filename=url.substring(url.indexOf('/')+1);
			page=Jsoup.connect(URLBASE+url).get();
//			saveHTML("old/"+filename,page.html());
			
			
			date=page.select("div.date").html();
			dateVal=parseDate(date);
			
			
			title=page.title();
			Elements tagEl=page.select("a.single-tag");
			tags=new ArrayList<String>();
			
			if(tagEl.size()>0) {
				for(Element theTag : tagEl){
//				println("tags "+theTag.html());
					tags.add(theTag.text());
					tagmap.addTag(theTag.text(), this);
				}
				
				Collections.sort(tags, new Comparator<String>() {
			    public int compare(String a, String b) {
			        return a.compareTo(b);
			    }
				});
				
				Elements tagsDiv=page.select("div.tags");
				if(tagsDiv.size()>0) tagsDiv.remove();
			}
			
			
			int cnt=0;
			Elements links=page.select("div.post-content p a");
			for(Element el : links) {
				String href=el.attr("href");
				if(href.indexOf("mwatz")!=-1) {
					if(cnt==0) printlnDivider("");
					cnt++;
					println("local link: "+href);
					href=href.substring(href.lastIndexOf('/')+1);
					if(!href.endsWith("html")) href+=".html";
					el.attr("href",href);
				}
			}
			Elements h3s=page.select("h3");
			if(h3s.size()>0) {
				Element theH3=h3s.first();
				String s=theH3.text();
//				theH3.after("<h3>"+h3s.text()+"</h3>");
				theH3.remove();
				
			}
						
			
//			println(page.title()+" | "+date);
//			println(html);

			Elements imgs=page.select("div.post-content img");
			String imgdat="";
			for(Element theImage : imgs) {
//					println(theImage.outerHtml());
				if(imgdat.length()>0) imgdat=imgdat+"\t";
				String src=theImage.attr("src");
				imgdat=imgdat+src;
				fetchImage(URLBASE+src);				
				theImage.removeAttr("src");
				theImage.attr("src","images/"+src.substring(src.lastIndexOf('/')+1));
			}
			
			html=page.select("div.post-content").html();
			
		}
		
		public void buildPage() {
			newpage=getBaseTemplate(title);
			contentHeader(newpage,title,date);			
			newpage.select("#contentMain").append(html);
			
			String nav="";
			if(prev!=null) nav+=div("navPrev",
					href(prev.filename,"&laquo; "+shorten(prev.title, 35)));
			if(next!=null) nav+=div("navNext",
					href(next.filename,shorten(next.title, 35)+" &raquo; "));
			nav=div("nav",nav+div("break", ""));
			newpage.select("#contentMain").append(nav);		
			
//			newpage.select("#contentMain").prepend(href(URLBASE+filename,
//					"original version"));
			
			if(tags.size()>0) {
				String s="";
				for(String tagname : tags) {
					TagList tl=tagmap.getTag(tagname);
					if(tl==null || tl.pages.size()<2) 
						s+="  <li>"+tagname+"</li>\n";
					else s+="  <li>"+href("tag-"+shortTag(tagname)+".html",tagname)+"</li>\n";
				}
				
				s="<p class='tagSidebarList'>Tags</p>\n"+ul(null,s);
				s=div("#tagSidebar",s);
				newpage.select("#content").append(s);
			}
			
			navScript(newpage,prev,next);
			analyticsScript(newpage);
			
			saveStrings("html/"+filename,new String[] {newpage.html()});
		}
		
	}
	
	
	class TagMap extends HashMap<String, TagList>{
		
		private ArrayList<TagList> sortedtags;

		public void addTag(String tag,Page p) {
			TagList list=get(tag);
			if(list==null) {
				list=new TagList(tag);
				list.pages.add(p);
				put(tag,list);
			}
			else list.pages.add(p);
		}
		
		public void print() {
			println(""+size());
			println(keySet().toString());
			Iterator<String> it=keySet().iterator();
			while (it.hasNext()) {
				TagList list=get(it.next());
				list.print();
			}
		}
		
		public TagList getTag(String tagname) {
			TagList tl=null;
			tl=get(tagname);
			return tl;
		}
		
		public int getTagOccurence(String tagname) {
			TagList tl=getTag(tagname);
			if(tl==null) return 0;
			return tl.pages.size();
		}
		
		public void finish() {
			sortedtags = new ArrayList<TagList>(values());
			Collections.sort(sortedtags, new Comparator<TagList>() {
				public int compare(TagList arg0, TagList arg1) {
					int diff=arg1.pages.size()-arg0.pages.size();
					if(diff==0) return arg0.tagname.compareTo(arg1.tagname);
					return diff;
				}
			});
			
			Document newpage=getBaseTemplate("Tags");
			String html="",inactivehtml="";
			
			for(TagList l : sortedtags) {
				l.sort();
//				println(l.pages.size()+" "+l.tagname);
				String sz="";
				if(l.pages.size()>1) {
					sz=" ["+l.pages.size()+"]";
					html+="  <li>"+href("tag-"+l.shortname+".html",l.tagname)+sz+"</li>\n";
				}
				else {
					inactivehtml+="  <li>"+l.tagname+"</li>\n";
				}
						
			}
			
			contentHeader(newpage,"Archive: Tag index",null);
			html="<ul class='index'>"+html+"</ul>\n";			
			newpage.select("#contentMain").append(html);
			newpage.select("#contentMain").append("<h3 class='indexInactive'>Tags used only once</h3>");
			inactivehtml="<ul class='index'>"+inactivehtml+"</ul>\n";			
			newpage.select("#contentMain").append(inactivehtml);
			analyticsScript(newpage);
			saveHTML("tags-all.html",newpage.html());
						
			for(TagList l : sortedtags) if(l.pages.size()>1) {
				newpage=getBaseTemplate("Tag: "+l.tagname);
				contentHeader(newpage,"Archive: Tag '"+l.tagname+"'",null);
				html="<ul class='pageIndex'>";
				for(Page pg : l.pages) {
					html+="<li>"+href(pg.filename, pg.title)+"</li>\n";
				}
				html+="</ul>\n";
				newpage.select("#contentMain").append(html);
				analyticsScript(newpage);
				saveHTML("tag-"+l.shortname+".html",newpage.html());
			}
		}
		
		

	}
	
	class TagList {
		String tagname,shortname;
		ArrayList<Page> pages;
		
		public TagList(String tag) {
			tagname=tag;
			shortname=shortTag(tagname);
			pages=new ArrayList<MwatzTumblr.Page>();
		}

		public void sort() {
			
			
		}

		public void print() {
			printlnDivider("Tag: "+tagname+" n="+pages.size());
			for(Page p : pages) println(p.filename);
			
		}

	}
	
	private void contentHeader(Document newpage, String h3,String date) {
		if(date!=null) 			
			newpage.select("#content").prepend("<div class='date'>"+date+"</date>\n");			
		newpage.select("#content").prepend("<h3>"+h3+"</h3>");
	}

	
	public static String [] months={
		"January","February","March","April",
		"May","June","July","August","September",
		"October","November","December"};
	
	public int[] parseDate(String date) {
		String tok[]=split(date.replaceAll(",",""), ' ');
		
		int[] dateVal=new int[3];
		dateVal[0]=Integer.parseInt(tok[1]);
		for(int i=0; i<12; i++) if(tok[0].startsWith(months[i])) dateVal[1]=i;
		dateVal[2]=Integer.parseInt(tok[2]);
		
		return dateVal;
	}
	
	public void saveHTML(String filename,String html) {
		saveStrings("html/"+filename,new String[] {html});
	}

	public void navScript(Document newpage, Page prev, Page next) {
		String script="";
		
		script+=
			"		function checkArrowKeys(e){\n"+
			"	    var arrs= [], key= window.event? event.keyCode: e.keyCode;\n"+
			"	    arrs[38]= 0;\n"+
			"	    arrs[37]= 1;\n"+
			"	    arrs[39]= 2;\n";
		script+="if(arrs[key] && arrs[key]==0) window.location.href='index.html';\n";
	  if(prev!=null ) script+="if(arrs[key] && arrs[key]==1) window.location.href='"+prev.filename+"';\n";
		if(next!=null ) script+="if(arrs[key] && arrs[key]==2) window.location.href='"+next.filename+"';\n";
		script+=
			"	 }\n" +
			"	 document.onkeydown=checkArrowKeys;\n";

		script="<script type='text/javascript'>"+script+"</script>\n";
		
		newpage.select("body").append(script);
		
	}

	public void analyticsScript(Document newpage) {		
		if(doAnalytics) {
			String script[]=loadStrings("html/templ/analytics.js");
			String theScript="";
			for(int i=0; i<script.length; i++) theScript+=script[i]+"\n";
			newpage.select("body").append(theScript);
		}
	}
	
	private String span(String css, String html) {
		return "<span class='"+css+"'>"+html+"</span>";
	}

	private String ul(String css, String html) {
		if(css!=null) css=" class='"+css+"'";
		else css="";
		return "<ul"+css+">\n"+html+"</ul>\n";
	}


	public String div(String css,String html) {
		if(css.startsWith("#")) css="id='"+css.substring(1)+"'";
		else css="class='"+css+"'";
		return "<div "+css+">"+html+"</div>\n";
	}
	
	public String href(String url, String text) {
		return "<a href='"+url+"'>"+text+"</a>";
	}

	String shortTag(String tag) {
		String shortened=""+tag;
		shortened=shortened.toLowerCase();
		shortened=shortened.replaceAll("[^A-Za-z]", "");
		return shortened;			
	}
	
	public static String shorten(String s, int len) {
		if (s==null) return null;
		if (s.length()>len) s=s.substring(0, len-2)+"..";
		return s;
	}

	private void printlnDivider(String string) {
		println("------------------------------------\n"+string);			
	}						


	
	static public void main(String args[]) {
		// PApplet.main(new String[] { "unlekker006default.AppDefault" });
		PApplet.main(new String[] { "scrape.MwatzTumblr" });

	}

}
