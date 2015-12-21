package aesthetiX;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.util.Vector;

public class Metrics {
//--------------------------------------------------------------------------------------------------
/**name:DENSITY
***importance: low
***ref:[2]
***formula:1-[(sum Ai)/ Aframe]*/
//---------------------------------------/
	public static float density(Element elmnt)// counts the widgets in the interface
	{
		int areaFrame = 0;
		int sumOfAreas = 0;
		NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
		for (int s = 0; s < nodeLst.getLength(); s++) 
		{
			//computer the total area of the frame
			int totalw = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridWidth"));
			int totalh = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridHeight"))-1;
			areaFrame = totalw * totalh;
			//System.out.println("area total=" + areaFrame);

			//scan the widgets of the frame one at a time
			NodeList kids = nodeLst.item(s).getChildNodes();
			for (int i = 0; i < kids.getLength(); i++) 
			{
				if (kids.item(i).getNodeName() != "#text") 
				{
					//System.out.println("child node n°" + i + " :"+ kids.item(i).getNodeName());
					//compute area of widget and add to sum
					int width = Integer.parseInt(((Element) kids.item(i)).getAttribute("gridwidth"));
					int height = Integer.parseInt(((Element) kids.item(i)).getAttribute("gridheight"));
					sumOfAreas = sumOfAreas + (width * height);
				}
			}
			//System.out.println("somme des areas=" + sumOfAreas);
		}
		return 1 - ((float) sumOfAreas / (float) areaFrame);
	}
//------------------------------------------------------------------------------------------------		
/**name:BALANCE
***importance: high
***ref:[5]
***formula:1-[(|BMvertical| + |BMhorizontal|)/]2*/
//--------------------------------------------------/
	public static float balance(Element elmnt) 
	{
		float Wl = 0;
		float Wr = 0;
		float Wt = 0;
		float Wb = 0;
		float BMvert = 0;
		float BMhoriz = 0;
		NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
		for (int s = 0; s < nodeLst.getLength(); s++)
		{
			//find the coordinates of vertical and horizontal axes of the frame
			float widthOfFrame=Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridWidth"));
			float heightOfFrame=Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridHeight"))-1;
			float vAxis = widthOfFrame/2;
			float hAxis = heightOfFrame/2;
			vAxis -= (1-(widthOfFrame%2))*0.5;
			hAxis -= (1-(heightOfFrame%2))*0.5;
			/*
			System.out.println("vAxis:"+vAxis);
			System.out.println("hAxis:"+hAxis);
			*/
			//scan the widgets of the frame one at a time
			NodeList kids = nodeLst.item(s).getChildNodes();
			for (int i = 0; i < kids.getLength(); i++)
			{
				if (kids.item(i).getNodeName() != "#text")
				{
					Element widt =(Element) kids.item(i);
					//size of the widget
					int width = Integer.parseInt(widt.getAttribute("gridwidth"));
					int height = Integer.parseInt(widt.getAttribute("gridheight"));
					//coords of the widget
					int x=Integer.parseInt(widt.getAttribute("gridx"));
					int y=Integer.parseInt(widt.getAttribute("gridy"));
					//coords of the center of the widget
					float centreX=width/2;centreX-=(1-(width%2))*0.5;
					float centreY=height/2;centreY-=(1-(height%2))*0.5;
					centreX+=x; centreY+=y;
					//distance from the center of widget to the axes.
					float vdist=Math.abs(centreX-vAxis);//Wl,Wr
		    		float hdist=Math.abs((centreY)-hAxis);//Wt,Wb
		    		//compute the weights of each quadrant of the screen
					if(centreX<vAxis)//Wl
		    		{	
						Wl+=(width*height)*vdist;
		    			if(centreY<hAxis)Wt+=(width*height)*hdist;//Wt
		    			else Wb+=(width*height)*hdist;//Wb
		    		}
		    		else//Wr
		    		{	
		    			Wr+=(width*height)* vdist;
		    			if(centreY<hAxis)Wt+=(width*height)*hdist;
		    			else Wb+=(width*height)* hdist;//Wb
		    		}
				
				/*
				System.out.println("--->item:"+kids.item(i).getNodeName());
				System.out.println("area:"+width*height);
				System.out.println("vdist:"+vdist);
				System.out.println("hdist:"+hdist);
				System.out.println("x:"+x);
				System.out.println("y:"+y);
				*/
				}
			}
		}
		
		BMvert=(Wl-Wr)/Math.max(Wl, Wr);
		BMhoriz=(Wt-Wb)/Math.max(Wt, Wb);
		/*
		System.out.println("Wl:"+Wl);
		System.out.println("Wr:"+Wr);
		System.out.println("Wt:"+Wt);
		System.out.println("Wb:"+Wb);
		System.out.println("-------");
		System.out.println("BMvert:"+BMvert);
		System.out.println("BMhoriz:"+BMhoriz);
		*/
		return 1 - ((Math.abs(BMvert) + Math.abs(BMhoriz)) / 2);
	}
//--------------------------------------------------------------------------------------------------/
/**name:UNITY
***importance: high
***ref:[2]
***formula:(|UMform| + |UMspace|)/2*/
//--------------------------------------/
	public static float unity(Element elmnt)
	{
		float areaFrame = 0;
		float sumOfAreas = 0;
		float nShape = 0;//number of widget types
		float nSize = 0;//number of sizes
		float aLayout = 0;//area of the "layout"
		float n=0;//number of objects on the frame
		float UMform=0;
		float UMspace=0;
		Vector<String> shapes=new Vector<String>();
		Vector<Integer> sizes= new Vector<Integer>();
		int extrLeftX,extrRightX,highestY,lowestY;
		
		NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
		for (int s = 0; s < nodeLst.getLength(); s++) 
		{
			//area of frame
			int totalw = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridWidth"));
			int totalh = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridHeight"))-1;
			areaFrame = totalw * totalh;
			/*System.out.println("totalw=" + totalw);
			System.out.println("totalh=" + totalh);
		 	System.out.println("area total=" + areaFrame);*/
			//initialize extreme coords of the "layout".
		 	extrLeftX=totalw-1; extrRightX=0; highestY=totalh-1;lowestY=0;
		 	//scan the widgets of the frame one at a time
			NodeList kids = nodeLst.item(s).getChildNodes();
			for (int i = 0; i < kids.getLength(); i++) 
			{
				if (kids.item(i).getNodeName() != "#text") 
				{
					Element widt =(Element) kids.item(i);
					//sum of areas
					int width = Integer.parseInt(widt.getAttribute("gridwidth"));
					int height = Integer.parseInt(widt.getAttribute("gridheight"));
					sumOfAreas = sumOfAreas + (width * height);
					//aLayout, compute the area of the layout
					int x=Integer.parseInt(widt.getAttribute("gridx"));
					int y=Integer.parseInt(widt.getAttribute("gridy"));
					if(x<extrLeftX)extrLeftX=x;
					if(x+width>extrRightX)extrRightX=x+width;
					//-
					if(y<highestY)highestY=y;
					if(y+height>lowestY)lowestY=y+height;
					//nSize, count number of sizes used
					if(!sizes.contains(new Integer((width*width)+height)))sizes.add(new Integer((width*width)+height));
					//nShape, the number of types of widgets used
					//System.out.println(widt.getChildNodes().item(1).getNodeName());
					if(!shapes.contains(widt.getChildNodes().item(1).getNodeName()))shapes.add(widt.getChildNodes().item(1).getNodeName());
					//n
					n++;
				}
			}
			
			/*
			System.out.println("somme des areas=" + sumOfAreas);
			System.out.println("extrLeftX=" + extrLeftX);
			System.out.println("extrRightX=" + extrRightX);
			System.out.println("highestY=" + highestY);
			System.out.println("lowestY=" + lowestY);
			 */
			aLayout= (extrRightX-extrLeftX)*(lowestY-highestY);
			nSize =sizes.size();
			nShape=shapes.size();
			/*
			System.out.println("area of the layout=" + aLayout);
			System.out.println("number of sizes=" + nSize);
			System.out.println("number of shapes=" + nShape);
			System.out.println("number of objects=" + n);
			 */
			UMform = 1 - ((nSize + nShape - 2) / (2 * n));
			UMspace = 1 - ((aLayout - sumOfAreas) / (areaFrame - sumOfAreas));
			/*
			System.out.println("UMform: " + UMform);
			System.out.println("UMspace: " + UMspace);
			 */
		}
		return (Math.abs(UMform)+Math.abs(UMspace))/2;
	}
//-------------------------------------------------------------------------------------------------/	
/**name:SYMMETRY
***importance: medium
***ref:[2]
***formula:1-[(|SYMvertical| + |SYMhorizontal| +|SYMradial|)/3]*/
	//---------------------------------------------------------/
	public static float symmetry(Element elmnt) 
	{
		float Xul=0, Xur=0, Xll=0, Xlr = 0;
		float Yul=0, Yur=0, Yll=0, Ylr = 0;
		float Hul=0, Hur=0, Hll=0, Hlr = 0;
		float Bul=0, Bur=0, Bll=0, Blr = 0;
		float Rul=0, Rur=0, Rll=0, Rlr = 0;
		float THul=0, THur=0, THll=0, THlr = 0;
		float SYMvert = 0;
		float SYMhoriz = 0;
		float SYMrad = 0;
		
		NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
		for (int s = 0; s < nodeLst.getLength(); s++)
		{
			//find the center of the frame
			float widthOfFrame=Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridWidth"));
			float heightOfFrame=Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridHeight"))-1;
			float xCentre = (widthOfFrame)/2;
			float yCentre = (heightOfFrame)/2;
			xCentre -= (1-(widthOfFrame%2))*0.5;
			yCentre -= (1-(heightOfFrame%2))*0.5;
			//System.out.println("xCentre:"+xCentre);
			//System.out.println("yCentre:"+yCentre);
			
			//iterate on the widgets of the frame
			NodeList kids = nodeLst.item(s).getChildNodes();
			for (int i = 0; i < kids.getLength(); i++)
			{
				if (kids.item(i).getNodeName() != "#text")
				{
					Element widt =(Element) kids.item(i);
					//size of the widget
					int width = Integer.parseInt(widt.getAttribute("gridwidth"));
					int height = Integer.parseInt(widt.getAttribute("gridheight"));
					//coordinates of the widget
					int x=Integer.parseInt(widt.getAttribute("gridx"));
					int y=Integer.parseInt(widt.getAttribute("gridy"));
					//center of the widget
					float centreX=width/2;centreX-=(1-(width%2))*0.5;
					float centreY=height/2;centreY-=(1-(height%2))*0.5;
					centreX+=x;centreY+=y;
					
					//find the quadrant the widget belongs to
		    		if(centreX<xCentre)//left
		    		{	
		    			//ul
		    			if(centreY<yCentre)
		    			{
		    				Xul += Math.abs(centreX-xCentre);
		    				Yul += Math.abs(centreY-yCentre);
		    				Hul += height;
		    				Bul += width;
		    				if((centreX-xCentre)!=0)
		    					THul+= Math.abs((centreY-yCentre)/(centreX-xCentre));
		    				Rul += Math.sqrt((Math.pow(centreX-xCentre, 2))+(Math.pow(centreY-yCentre, 2)));
		    			}
		    			//ll
		    			else{
		    				Xll += Math.abs(centreX-xCentre);
		    				Yll += Math.abs(centreY-yCentre);
		    				Hll += height;
		    				Bll += width;
		    				if((centreX-xCentre)!=0)
		    					THll+= Math.abs((centreY-yCentre)/(centreX-xCentre));
		    				Rll += Math.sqrt((Math.pow(centreX-xCentre, 2))+(Math.pow(centreY-yCentre, 2)));
		    			}
		    		}
		    		else//right
		    		{	
		    			//ur
		    			if(centreY<yCentre){
		    				Xur += Math.abs(centreX-xCentre);
		    				Yur += Math.abs(centreY-yCentre);
		    				Hur += height;
		    				Bur += width;
		    				if((centreX-xCentre)!=0)
		    					THur+= Math.abs((centreY-yCentre)/(centreX-xCentre));
		    				Rur += Math.sqrt((Math.pow(centreX-xCentre, 2))+(Math.pow(centreY-yCentre, 2)));
		    			}
		    			//lr
		    			else {
		    				Xlr += Math.abs(centreX-xCentre);
		    				Ylr += Math.abs(centreY-yCentre);
		    				Hlr += height;
		    				Blr += width;
		    				if((centreX-xCentre)!=0)
		    					THlr+= Math.abs((centreY-yCentre)/(centreX-xCentre));
		    				Rlr += Math.sqrt((Math.pow(centreX-xCentre, 2))+(Math.pow(centreY-yCentre, 2)));
		    			}
		    		}
				}
			}
		}
		/**Let's normalize the values*/
			//take the maximum...
		float max=Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Xul, Xur), Xll), Xlr), Yul), Yur), Yll), Ylr), Hul), Hur), Hll), Hlr), Bul), Bur), Bll), Blr), THul), THur), THll), THlr), Rul), Rur), Rll), Rlr);
			//and normalize...
		Xul*=(1/max); Xur*=(1/max); Xll*=(1/max); Xlr *=(1/max);
		Yul*=(1/max); Yur*=(1/max); Yll*=(1/max); Ylr *=(1/max);
		Hul*=(1/max); Hur*=(1/max); Hll*=(1/max); Hlr *=(1/max);
		Bul*=(1/max); Bur*=(1/max); Bll*=(1/max); Blr *=(1/max);
		THul*=(1/max); THur*=(1/max); THll*=(1/max); THlr *=(1/max);
		Rul*=(1/max); Rur*=(1/max); Rll*=(1/max); Rlr *=(1/max);
		/*
		System.out.println("-------");
		System.out.println("Xul:"+Xul);
		System.out.println("Xur:"+Xur);
		System.out.println("Xll:"+Xll);
		System.out.println("Xlr:"+Xlr);
		System.out.println("Yul:"+Yul);
		System.out.println("Yur:"+Yur);
		System.out.println("Yll:"+Yll);
		System.out.println("Ylr:"+Ylr);
		System.out.println("Hul:"+Hul);
		System.out.println("Hur:"+Hur);
		System.out.println("Hll:"+Hll);
		System.out.println("Hlr:"+Hlr);
		System.out.println("Bul:"+Bul);
		System.out.println("Bur:"+Bur);
		System.out.println("Bll:"+Bll);
		System.out.println("Blr:"+Blr);
		System.out.println("THul:"+THul);
		System.out.println("THur:"+THur);
		System.out.println("THll:"+THll);
		System.out.println("THlr:"+THlr);
		System.out.println("Rul:"+Rul);
		System.out.println("Rur:"+Rur);
		System.out.println("Rll:"+Rll);
		System.out.println("Rlr:"+Rlr);
		*/
		SYMvert =	((Math.abs(Xul-Xur)+Math.abs(Xll-Xlr))+Math.abs(Yul-Yur)+Math.abs(Yll-Ylr)+
					(Math.abs(Hul-Hur)+Math.abs(Hll-Hlr))+Math.abs(Bul-Bur)+Math.abs(Bll-Blr)+
					(Math.abs(THul-THur)+Math.abs(THll-THlr))+Math.abs(Rul-Rur)+Math.abs(Rll-Rlr))
					/12;
		SYMhoriz =	((Math.abs(Xul-Xll)+Math.abs(Xur-Xlr))+Math.abs(Yul-Yll)+Math.abs(Yur-Ylr)+
					(Math.abs(Hul-Hll)+Math.abs(Hur-Hlr))+Math.abs(Bul-Bll)+Math.abs(Bur-Blr)+
					(Math.abs(THul-THll)+Math.abs(THur-THlr))+Math.abs(Rul-Rll)+Math.abs(Rur-Rlr))
					/12;
		SYMrad =	((Math.abs(Xul-Xlr)+Math.abs(Xur-Xll))+Math.abs(Yul-Ylr)+Math.abs(Yur-Yll)+
					(Math.abs(Hul-Hlr)+Math.abs(Hur-Hll))+Math.abs(Bul-Blr)+Math.abs(Bur-Bll)+
					(Math.abs(THul-THlr)+Math.abs(THur-THll))+Math.abs(Rul-Rlr)+Math.abs(Rur-Rll))
					/12;
		/*
		System.out.println("-------");
		System.out.println("SYMvert:"+SYMvert);
		System.out.println("SYMMhoriz:"+SYMhoriz);
		System.out.println("SYMrad:"+SYMrad);
		*/
		return 1 - ((Math.abs(SYMvert) + Math.abs(SYMhoriz) + Math.abs(SYMrad)) / 3);
	}
	
//--------------------------------------------------------------------------------------------------/
/**name:ALIGNMENT
***importance: high
***ref:[16]
***formula:...*/
//--------------------------------------/
	public static float alignment(Element elmnt)
	{

		float nVap = 0;//number of vertical alignment points
		float nHap = 0;//number of horizontal alignment points
		float n=0;//number of objects on the frame
		Vector<Integer> vertAlignPoints=new Vector<Integer>();
		Vector<Integer> horizAlignPoints= new Vector<Integer>();
		
		NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
		for (int s = 0; s < nodeLst.getLength(); s++) 
		{
		 	//scan the widgets of the frame one at a time
			NodeList kids = nodeLst.item(s).getChildNodes();
			for (int i = 0; i < kids.getLength(); i++) 
			{
				if (kids.item(i).getNodeName() != "#text") 
				{
					Element widt =(Element) kids.item(i);
					//width & height
					int width = Integer.parseInt(widt.getAttribute("gridwidth"));
					int height = Integer.parseInt(widt.getAttribute("gridheight"));
					//coordinates of widget
					int x=Integer.parseInt(widt.getAttribute("gridx"));
					int y=Integer.parseInt(widt.getAttribute("gridy"));
					
					//add new vertical alignment points if necessary
					if(!vertAlignPoints.contains(x))vertAlignPoints.add(x);
					if(!vertAlignPoints.contains(x+width))vertAlignPoints.add(x+width);
					//add new horizontal alignment points if necessary
					if(!horizAlignPoints.contains(y))horizAlignPoints.add(y);
					if(!horizAlignPoints.contains(y+height))horizAlignPoints.add(y+height);			
					//n
					n++;
				}
			}
			/*
			System.out.println("somme des areas=" + sumOfAreas);
			 */
			nVap =vertAlignPoints.size();
			nHap=horizAlignPoints.size();
			/*
			System.out.println("area of the layout=" + aLayout);
			 */
		}
		return Math.min(1,(4*n-(nHap*nVap/n))/(4*n-4));
	}
//--------------------------------------------------------------------------------------------------/
/**name:GROUPING
***importance: medium
***ref:[16]
***formula:...*/
//--------------------------------------/
		public static float grouping(Element elmnt)
		{
			int areaFrame = 0;//area of the screen
			Vector<Vector<Integer>> vertAlignPoints = new Vector<Vector<Integer>>();
			Vector<Vector<Integer>> horizAlignPoints = new Vector<Vector<Integer>>();
			Vector<String> shapes = new Vector<String>();
			Vector<Integer> sumOfAreas = new Vector<Integer>();//sum of areas of objects of the same type
			Vector<Integer> Ni = new Vector<Integer>();//number of objects of a certain type
			Vector<Integer> extrLeftX= new Vector<Integer>(); 
			Vector<Integer> extrRightX= new Vector<Integer>();
			Vector<Integer> highestY= new Vector<Integer>();
			Vector<Integer> lowestY = new Vector<Integer>();
			//Let's initialize the vectors.
			/*
			Vector<Integer> temp1 = new Vector<Integer>();temp1.add(0);
			Vector<Integer> temp2 = new Vector<Integer>();temp2.add(0);
			vertAlignPoints.add(temp1);
			horizAlignPoints.add(temp2);
			*/
			sumOfAreas.add(0);
			Ni.add(0);
			extrLeftX.add(1000000);
			extrRightX.add(0);
			highestY.add(1000000);
			lowestY.add(0);
			
			float GR=0;
			NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
			for (int s = 0; s < nodeLst.getLength(); s++) 
			{
				//area of frame
				int totalw = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridWidth"));
				int totalh = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridHeight"))-1;
				areaFrame = totalw * totalh;
			 	//System.out.println("area total=" + areaFrame);
			 	//Let's first count the number of types of widgets----------------------------------
				NodeList kids = nodeLst.item(s).getChildNodes();
				int j=0;
				for (int i = 0; i < kids.getLength(); i++)
				{
					if (kids.item(i).getNodeName() != "#text")
					{
						Element widt =(Element) kids.item(i);
						String type=widt.getChildNodes().item(1).getNodeName()+widt.getAttribute("gridwidth")+widt.getAttribute("gridheight");
						int width = Integer.parseInt(widt.getAttribute("gridwidth"));
						int height = Integer.parseInt(widt.getAttribute("gridheight"));
						//int j=0;
						if(!shapes.contains(type))
							{
								//we store the distinct types in a vector
								shapes.add(type);
								//we extend the vectors
								Vector<Integer> tmp1 = new Vector<Integer>();//tmp1.add(0);
								Vector<Integer> tmp2 = new Vector<Integer>();//tmp2.add(0);
								vertAlignPoints.add(tmp1);
								horizAlignPoints.add(tmp2);
								//Ni.add(0);
								//we count the number of objects in each type.
								j=shapes.indexOf(type);
								Ni.add(j, 1);
								//we sum the areas of objects of the same type.
								sumOfAreas.add(j,(width * height));
								
								
								
								//aLayout|compute the area of the layout formed by the objects of a given type
								int x=Integer.parseInt(widt.getAttribute("gridx"));
								int y=Integer.parseInt(widt.getAttribute("gridy"));
								//System.out.println("exl before:"+extrLeftX.get(j).intValue());
								extrLeftX.add(j,x);
								//System.out.println("exl after:"+extrLeftX.get(j).intValue());
								extrRightX.add(j,x+width);
								//-
								highestY.add(j, y);
								lowestY.add(j,y+height);
								
								
								//add new vertical alignment points if necessary
								//System.out.println("j:"+j +" x:"+x);
								vertAlignPoints.get(j).add(x);
								//System.out.println("vertAl size:"+vertAlignPoints.size());
								//vertAlignPoints.get(j).add(x);
								vertAlignPoints.get(j).add(x+width);
								//add new horizontal alignment points if necessary
								horizAlignPoints.get(j).add(y);
								horizAlignPoints.get(j).add(y+height);			

								
							}
						else
							{
								//we count the number of objects in each type.
								j=shapes.indexOf(type);
								Ni.set(j, Ni.get(j).intValue()+1);
								//we sum the areas of objects of the same type.
								sumOfAreas.set(j,sumOfAreas.get(j).intValue()+(width * height));
								
								
								//aLayout|compute the area of the layout formed by the objects of a given type
								int x=Integer.parseInt(widt.getAttribute("gridx"));
								int y=Integer.parseInt(widt.getAttribute("gridy"));
								//System.out.println("exl before:"+extrLeftX.get(j).intValue());
								if(x<extrLeftX.get(j).intValue())extrLeftX.set(j,x);
								//System.out.println("exl after:"+extrLeftX.get(j).intValue());
								if(x+width>extrRightX.get(j).intValue())extrRightX.set(j,x+width);
								//-
								if(y<highestY.get(j).intValue())highestY.set(j, y);
								if(y+height>lowestY.get(j).intValue())lowestY.set(j,y+height);
								
								
								
								//add new vertical alignment points if necessary
								//System.out.println("j:"+j +" x:"+x);
								if(!vertAlignPoints.get(j).contains(x))vertAlignPoints.get(j).add(x);
								//System.out.println("vertAl size:"+vertAlignPoints.size());
								//vertAlignPoints.get(j).add(x);
								if(!vertAlignPoints.get(j).contains(x+width))vertAlignPoints.get(j).add(x+width);
								//add new horizontal alignment points if necessary
								if(!horizAlignPoints.get(j).contains(y))horizAlignPoints.get(j).add(y);
								if(!horizAlignPoints.get(j).contains(y+height))horizAlignPoints.get(j).add(y+height);			

							}
						//initialize extreme coordinates of the "layout" formed by the objects of a given type
						////nShape=shapes.size();
						//extrLeftX=new Vector<Integer>((int)nShape);
						//extrRightX=new Vector<Integer>((int)nShape);
						//highestY=new Vector<Integer>((int)nShape);
						//lowestY=new Vector<Integer>((int)nShape);
						/*if(extrLeftX.get(0)==1000000)
						{
							for(int k=0;k<nShape;k++)
							{
								extrLeftX.add(k,totalw-1); 
								extrRightX.add(k,0); 
								highestY.add(k,totalh-1);
								lowestY.add(k, 0);
							}
						}*/
						/*
						//aLayout|compute the area of the layout formed by the objects of a given type
						int x=Integer.parseInt(widt.getAttribute("gridx"));
						int y=Integer.parseInt(widt.getAttribute("gridy"));
						System.out.println("exl before:"+extrLeftX.get(j).intValue());
						if(x<extrLeftX.get(j).intValue())extrLeftX.add(j,x);
						System.out.println("exl after:"+extrLeftX.get(j).intValue());
						if(x+width>extrRightX.get(j).intValue())extrRightX.add(j,x+width);
						//-
						if(y<highestY.get(j).intValue())highestY.add(j, y);
						if(y+height>lowestY.get(j).intValue())lowestY.add(j,y+height);
						*/
						
						/*
						//add new vertical alignment points if necessary
						//System.out.println("j:"+j +" x:"+x);
						if(!vertAlignPoints.get(j).contains(x))vertAlignPoints.get(j).add(x);
						//System.out.println("vertAl size:"+vertAlignPoints.size());
						//vertAlignPoints.get(j).add(x);
						if(!vertAlignPoints.get(j).contains(x+width))vertAlignPoints.get(j).add(x+width);
						//add new horizontal alignment points if necessary
						if(!horizAlignPoints.get(j).contains(y))horizAlignPoints.get(j).add(y);
						if(!horizAlignPoints.get(j).contains(y+height))horizAlignPoints.get(j).add(y+height);			
						*/

						//j++;
					}
				}
				
				//now that we have extracted the data that we need let's compute the formula
				for(int k=0;k<shapes.size();k++)
				{
					//
					/*
					System.out.println("--------------------------- ");
					System.out.println("Now type...: "+shapes.get(k));
					*/
					int aLayout = (extrRightX.get(k).intValue()-extrLeftX.get(k).intValue())*(lowestY.get(k).intValue()-highestY.get(k).intValue());
					/*
					System.out.println("elX:"+extrLeftX.get(k).intValue());
					System.out.println("eRX:"+extrRightX.get(k).intValue());
					System.out.println("hY:"+highestY.get(k).intValue());
					System.out.println("lY:"+lowestY.get(k).intValue());
					System.out.println("aLayout:"+aLayout);
					*/
					int nVap = vertAlignPoints.get(k).size();
					//System.out.println("nVap:"+nVap);
					int nHap = horizAlignPoints.get(k).size();
					/*
					System.out.println("nHap:"+nHap);
					System.out.println("areaFrame:"+areaFrame);
					System.out.println("sum of areas:"+sumOfAreas.get(k).intValue());
					*/
					//weighted sum of unity and alignment of widgets of the same type.
					//GR+=(0.85f)*UMspace(aLayout,sumOfAreas.get(k).intValue(),areaFrame)+(0.15f)*AL(Ni.get(k).intValue(), nHap, nVap);
					GR+=(0.5f)*UMspace(aLayout,sumOfAreas.get(k).intValue(),areaFrame)+(0.5f)*AL(Ni.get(k).intValue(), nHap, nVap);
				}
			}
			return GR/shapes.size();
		}
		public static float UMspace(int aLayout, int sumOfAreas, int aFrame)
		{
			/*System.out.println("UMspace=" + (1 - Math.abs(((float)(aLayout - sumOfAreas)*15.0f/(float)(aFrame - sumOfAreas)))));
			return 1 - Math.abs(((float)(aLayout - sumOfAreas)*15.0f/(float)(aFrame - sumOfAreas)));*/
			float um = 1 - Math.abs(((float)(aLayout - sumOfAreas)/(float)(aFrame - sumOfAreas)));
			//if(aLayout- sumOfAreas != 0)um = um * (0.1f - 0.9f /(aLayout - sumOfAreas));
			//System.out.println("UMspace=" + um);
			return um;
		}
		public static float AL(int Ni, int nHap, int nVap)
		{
			/*
			System.out.println("AL=" + (Math.min(1 , ((4*Ni - ( (float)nHap*nVap / (float)Ni)) / (float)(4*Ni - 4)))));
			return  Math.min(1 , ((4*Ni - ( (float)nHap*nVap / (float)Ni)) / (float)(4*Ni - 4)));
			*/
			/*
			System.out.println("nHap: " + nHap);
			System.out.println("nVap: " + nVap);
			*/
			//System.out.println("Ni: " + Ni);
			//System.out.println("AL=" +( 1 - (((float)(nHap*nVap)) / (float)(4*Ni*Ni))));
			
			if(((Ni+1<= nVap)&&(nVap<= 2*Ni))&&((Ni+1 <= nHap)&&(nHap<= 2*Ni)))return 0;
			else{
				if(nVap==2 || nHap==2) return 1;//return 1- (((float)(nHap*nVap)) / (float)(4*Ni*Ni));
				else return 0.5f;
			}
		}
	
//--------------------------------------------------------------------------------------------------/
/**name:SIMPLICITY
***importance: low
***ref:[16][2]
***formula:...*/
//--------------------------------------/
	public static float simplicity(Element elmnt)
	{

		float nVap = 0;//number of vertical alignment points
		float nHap = 0;//number of horizontal alignment points
		float n=0;//number of objects on the frame
		Vector<Integer> vertAlignPoints=new Vector<Integer>();
		Vector<Integer> horizAlignPoints= new Vector<Integer>();
		
		NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
		for (int s = 0; s < nodeLst.getLength(); s++) 
		{
		 	//scan the widgets of the frame one at a time
			NodeList kids = nodeLst.item(s).getChildNodes();
			for (int i = 0; i < kids.getLength(); i++) 
			{
				if (kids.item(i).getNodeName() != "#text") 
				{
					Element widt =(Element) kids.item(i);
					//width & height
					int width = Integer.parseInt(widt.getAttribute("gridwidth"));
					int height = Integer.parseInt(widt.getAttribute("gridheight"));
					//coordinates of widget
					int x=Integer.parseInt(widt.getAttribute("gridx"));
					int y=Integer.parseInt(widt.getAttribute("gridy"));
					
					//add new vertical alignment points if necessary
					if(!vertAlignPoints.contains(x))vertAlignPoints.add(x);
					if(!vertAlignPoints.contains(x+width))vertAlignPoints.add(x+width);
					//add new horizontal alignment points if necessary
					if(!horizAlignPoints.contains(y))horizAlignPoints.add(y);
					if(!horizAlignPoints.contains(y+height))horizAlignPoints.add(y+height);			
					//n
					n++;
				}
			}
			/*
			System.out.println("somme des areas=" + sumOfAreas);
			 */
			nVap =vertAlignPoints.size();
			nHap=horizAlignPoints.size();
			/*
			System.out.println("area of the layout=" + aLayout);
			 */
		}
		return 3/(nHap+nVap+n);
	}
//--------------------------------------------------------------------------------------------------/
/**name:ECONOMY
***importance: low
***ref:[2][16]
***formula:(|UMform| + |UMspace|)/2*/
//--------------------------------------/
		public static float economy(Element elmnt)
		{
			float sumOfAreas = 0;
			float nShape = 0;//number of widget types
			float nSize = 0;//number of sizes
			
			float n=0;//number of objects on the frame
			Vector<String> shapes=new Vector<String>();
			Vector<Integer> sizes= new Vector<Integer>();
			
			NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
			for (int s = 0; s < nodeLst.getLength(); s++) 
			{
				//scan the widgets of the frame one at a time
				NodeList kids = nodeLst.item(s).getChildNodes();
				for (int i = 0; i < kids.getLength(); i++) 
				{
					if (kids.item(i).getNodeName() != "#text") 
					{
						Element widt =(Element) kids.item(i);
						//sum of areas
						int width = Integer.parseInt(widt.getAttribute("gridwidth"));
						int height = Integer.parseInt(widt.getAttribute("gridheight"));
						sumOfAreas = sumOfAreas + (width * height);
						
						//nSize, count number of sizes used
						if(!sizes.contains(new Integer((width*width)+height)))sizes.add(new Integer((width*width)+height));
						//nShape, the number of types of widgets used
						//System.out.println(widt.getChildNodes().item(1).getNodeName());
						if(!shapes.contains(widt.getChildNodes().item(1).getNodeName()))shapes.add(widt.getChildNodes().item(1).getNodeName());
						//n
						n++;
					}
				}
								
				nSize =sizes.size();
				nShape=shapes.size();
				/*
				System.out.println("number of sizes=" + nSize);
				System.out.println("number of shapes=" + nShape);
				System.out.println("number of objects=" + n);
				 */
							}
			return 2/(nSize+nShape);
		}
//-------------------------------------------------------------------------------------------------/	
/**name:REPARTITION
***importance: medium
***ref:[2][16]
***formula:1-[(|SYMvertical| + |SYMhorizontal| +|SYMradial|)/3]*/
//---------------------------------------------------------/
	public static double repartition(Element elmnt) 
	{
   		int Nul=0, Nur=0, Nll=0, Nlr = 0, n = 0;
		double W = 0;
		double Wmax = 0;
				
		NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
		for (int s = 0; s < nodeLst.getLength(); s++)
		{
			//find the center of the frame
			float widthOfFrame=Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridWidth"));
			float heightOfFrame=Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridHeight"))-1;
			float xCentre = (widthOfFrame)/2;
			float yCentre = (heightOfFrame)/2;
			xCentre -= (1-(widthOfFrame%2))*0.5;
			yCentre -= (1-(heightOfFrame%2))*0.5;
			//System.out.println("xCentre:"+xCentre);
			//System.out.println("yCentre:"+yCentre);
			
			//iterate on the widgets of the frame
			NodeList kids = nodeLst.item(s).getChildNodes();
			for (int i = 0; i < kids.getLength(); i++)
			{
				if (kids.item(i).getNodeName() != "#text")
				{
					Element widt =(Element) kids.item(i);
					//size of the widget
					int width = Integer.parseInt(widt.getAttribute("gridwidth"));
					int height = Integer.parseInt(widt.getAttribute("gridheight"));
					//coordinates of the widget
					int x=Integer.parseInt(widt.getAttribute("gridx"));
					int y=Integer.parseInt(widt.getAttribute("gridy"));
					//center of the widget
					float centreX=width/2;centreX-=(1-(width%2))*0.5;
					float centreY=height/2;centreY-=(1-(height%2))*0.5;
					centreX+=x;centreY+=y;
					
					//find the quadrant the widget belongs to
		    		if(centreX<xCentre)//left
		    		{	
		    			//ul
		    			if(centreY<yCentre)
		    			{
		    				Nul++;
		    			}
		    			//ll
		    			else{
		    				Nll++;
		    				}
		    		}
		    		else//right
		    		{	
		    			//ur
		    			if(centreY<yCentre){
		    				Nur++;
		    			}
		    			//lr
		    			else {
		    				Nlr++;
		    			}
		    		}
		    		n++;
				}
			}
		}
		/*
		System.out.println("-------");
		System.out.println("Nul:"+Nul);
		System.out.println("Nur:"+Nur);
		System.out.println("Nll:"+Nll);
		System.out.println("Nlr:"+Nlr);
		System.out.println("n:"+n);
		*/
		W =	Factorial(n).divide((Factorial(Nul).multiply(Factorial(Nll)).multiply(Factorial(Nur)).multiply(Factorial(Nlr)))).doubleValue();
		//W =Factorial(n)/(Factorial(Nul)*Factorial(Nll)*Factorial(Nur)*Factorial(Nlr));
		Wmax =	Factorial(n).divide(Factorial(n/4).multiply(Factorial(n/4)).multiply(Factorial(n/4)).multiply(Factorial(n/4))).doubleValue();
		//Wmax =Factorial(n)/(Factorial(n/4)*Factorial(n/4)*Factorial(n/4)*Factorial(n/4));
		
		//Wmax =Factorial(n)/(Factorial(n/2.5)*Factorial(n/5)*Factorial(n/4)*Factorial(n/6.666666666666));
	
		/*
		System.out.println("-------");
		System.out.println("Nul! "+Factorial(Nul));
		System.out.println("Nll! "+Factorial(Nll));
		System.out.println("Nur! "+Factorial(Nur));
		System.out.println("Nlr! "+Factorial(Nlr));
		System.out.println("n "+Factorial(n));
		System.out.println("W:"+W);
		System.out.println("Wmax"+Wmax);
		*/
		return W/Wmax;
	}
	/*public static long Factorial(int i){
    
		long n = 1;
		for (int j=1; j<=i; j++) {
			n = n*j;
		}
		return n;
	}*/
	
	public static BigDecimal Factorial(int t){
	    
	     BigDecimal n = BigDecimal.ONE;
	     for (int i=1; i<=t; i++)
	      {
	        n = n.multiply(BigDecimal.valueOf(i));
	      }
	       return n;
	    }
	    
	
//--------------------------------------------------------------------------------------------------/
/**name:PROPORTION
***importance: high
***ref:[2]
***formula:(|UMform| + |UMspace|)/2*/
//--------------------------------------/
		public static float proportion(Element elmnt)
		{
			//float areaFrame = 0;
			//float sumOfAreas = 0;
			//float nShape = 0;//number of widget types
			//float nSize = 0;//number of sizes
			//float aLayout = 0;//area of the "layout"
			float n=0;//number of objects on the frame
			float PMobject=0;
			float PMlayout=0;
			//Vector<String> shapes=new Vector<String>();
			Vector<Float> props= new Vector<Float>();
			int extrLeftX,extrRightX,highestY,lowestY;
			
			NodeList nodeLst = elmnt.getElementsByTagName("gridBagBox");
			for (int s = 0; s < nodeLst.getLength(); s++) 
			{
				//area of frame
				int totalw = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridWidth"));
				int totalh = Integer.parseInt(((Element) nodeLst.item(s)).getAttribute("gridHeight"))-1;
				//areaFrame = totalw * totalh;
			 	//System.out.println("area total=" + areaFrame);
				//initialize extreme coords of the "layout".
			 	extrLeftX=totalw-1; extrRightX=0; highestY=totalh-1;lowestY=0;
			 	//scan the widgets of the frame one at a time
				NodeList kids = nodeLst.item(s).getChildNodes();
				for (int i = 0; i < kids.getLength(); i++) 
				{
					if (kids.item(i).getNodeName() != "#text") 
					{
						Element widt =(Element) kids.item(i);
				//		//sum of areas
						int width = Integer.parseInt(widt.getAttribute("gridwidth"));
						int height = Integer.parseInt(widt.getAttribute("gridheight"));
				//		sumOfAreas = sumOfAreas + (width * height);
						//aLayout, compute the area of the layout
						int x=Integer.parseInt(widt.getAttribute("gridx"));
						int y=Integer.parseInt(widt.getAttribute("gridy"));
						if(x<extrLeftX)extrLeftX=x;
						if(x+width>extrRightX)extrRightX=x+width;
						//-
						if(y<highestY)highestY=y;
						if(y+height>lowestY)lowestY=y+height;
						//nSize, count number of sizes used
						props.add(new Float((float)height/(float)width));
						
						n++;
					}
				}
				/*
				System.out.println("extrLeftX=" + extrLeftX);
				System.out.println("extrRightX=" + extrRightX);
				System.out.println("highestY=" + highestY);
				System.out.println("lowestY=" + lowestY);
				 */
				//aLayout= (extrRightX-extrLeftX)*(lowestY-highestY);
				//nSize =sizes.size();
				/*
				System.out.println("area of the layout=" + aLayout);
				System.out.println("number of sizes=" + nSize);
				System.out.println("number of shapes=" + nShape);
				System.out.println("number of objects=" + n);
				 */
				//let's compute PMobject
				float sum=0;
				for(int i=0;i<n;i++){
					float pi=props.get(i);
					if(pi>1)pi=1/pi;
					//System.out.println("pi: " + pi);
					sum+=1-((Math.min(Math.abs(1 - pi), Math.min(Math.abs(1/1.414f - pi), Math.min(Math.abs(1/1.618f - pi), Math.min(Math.abs(1/1.732f - pi), Math.abs(0.5f - pi))))))/0.5f);
				}
				//System.out.println("size of props=" + props.size());
				PMobject = sum/n;
				
				//let's compute PMlayout
				float playout=(float)(lowestY-highestY)/(float)(extrRightX-extrLeftX);
				//float playout=(float)totalh/(float)totalw;
				if(playout>1)playout=1/playout;
				PMlayout=1-((Math.min(Math.abs(1-playout), Math.min(Math.abs(1/1.414f - playout), Math.min(Math.abs(1/1.618f - playout), Math.min(Math.abs(1/1.732f-playout), Math.abs(0.5f-playout))))))/0.5f);
				
				/*
				System.out.println("PMobject: " + PMobject);
				System.out.println("PMlayout: " + PMlayout);
				 */
			}
			return (PMobject+PMlayout)/2;
		}	

}
