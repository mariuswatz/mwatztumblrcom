package scrape;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import processing.core.PApplet;
import processing.core.PImage;
import unlekker.app.*;
import unlekker.util.*;

public class FFFFoundScrape extends App {
	Document doc;

	ArrayList<String> images;
	
	public void setup() {
		size(600,600);
		appRenderer=JAVA2D;
		readHTML();
	}
	
	private void readHTML() {
		try {
			doc=Jsoup.connect("http://ffffound.com/home/watz/found/").get();
			String title = doc.title();
			System.out.println("title "+title);
			
			images=new ArrayList<String>();
			
			Elements img = doc.select("div.description a"); // a with href
			for(Element el : img) {
				String s=el.html();
				System.out.println(el.attr("href")+" | "+s);
				int pos=s.indexOf("http");
				if(pos!=-1) {
					s=s.substring(pos);
					s=s.substring(0,s.indexOf("\""));
					images.add(s);
				}
			}

			Elements page=doc.select("span.paging a"); // a with href
			for(Element el : page) {
				if(el.html().indexOf("Next")==-1)
					System.out.println(el.attr("href")+"|"+ el.html());
			}

//			int id=0;
//			for(String s : images) try {
//				log((id++)+" "+s);
//				String filename=s.substring(s.lastIndexOf('/')+1);
//				if(!new File("data/"+filename).exists()) {
//					PImage image=loadImage(s); 
//					image.save("data/"+filename);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void draw() {
		
	}

	static public void main(String args[]) {
		// PApplet.main(new String[] { "unlekker006default.AppDefault" });
		PApplet.main(new String[] { "scrape.FFFFoundScrape" });

	}

	
	class ImageFetcher extends Thread {
		String url,filename;
		
		
	}
}
