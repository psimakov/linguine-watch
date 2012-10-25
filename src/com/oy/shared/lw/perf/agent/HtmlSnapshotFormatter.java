package com.oy.shared.lw.perf.agent;

final public class HtmlSnapshotFormatter {

	private String valueSeparator;
	private String namePrefix;
	private String namePostfix;	
	private String valuesPrefix;
	private String valuesPostfix;	
	private String cellPrefix;
	private String cellPostfix;
	private String headCellPrefix;
	private String headRowPrefix;
	private String oddRowPrefix;
	private String evenRowPrefix;
	private String rowPostfix;
	private String snapshotPrefix;
	private String snapshotPostfix;
	private String emptyCellText;
		
	public HtmlSnapshotFormatter(){
		
		setSnapshotPrefix(
			"<html><body style='font-family:Arial; font-size:8pt'>" + 
			"Powered by <a href='http://www.softwaresecretweapons.com/jspwiki/Wiki.jsp?page=LinguineWatch'>Linguine Watch</a> Performance Monitoring Library<br>&nbsp;<br>" + 
			"<table border='1' cellpadding='4' cellspacing='0' style='border-collapse: collapse; font-family:Arial; font-size:8pt'>"
		);
		setSnapshotPostfix("</table></body></html>");
		
		setHeadRowPrefix("<tr bgcolor='#eaffea'>");
		setOddRowPrefix("<tr bgcolor='#eaeaea'>");
		setEvenRowPrefix("<tr>");
		
		setRowPostfix("</tr>\n");

		setHeadCellPrefix("<td align='center'>");
		setValueCellPrefix("<td nowrap align='center'>");
		
		setCellPostfix("</td>");
		
		setValuesPrefix("");
		setValuesPostfix("");
		
		setValueSeparator("&nbsp;: ");
		setEmptyCellText("-");		
		
		setNamePrefix("<b>");
		setNamePostfix("</b><br>");			
	}
	
	public void setValueSeparator(String value){
		valueSeparator = value;
	}
	
	public String getValueSeparator(){
		return valueSeparator;
	}	
	
	public void setValueCellPrefix(String value){
		cellPrefix = value;
	}
	
	public void setHeadCellPrefix(String value){
		headCellPrefix = value;
	}	
	
	public String getCellPrefix(){
		return cellPrefix;
	}
	
	public String getHeadCellPrefix(){
		return headCellPrefix;
	}	
	
	public void setValuesPrefix(String value){
		valuesPrefix = value;
	}
	
	public String getValuesPrefix(){
		return valuesPrefix;
	}	
	
	public void setNamePrefix(String value){
		namePrefix = value;
	}
	
	public String getNamePrefix(){
		return namePrefix;
	}	
	
	public void setCellPostfix(String value){
		cellPostfix = value;
	}
	
	public String getCellPostfix(){
		return cellPostfix;
	}
	
	public void setValuesPostfix(String value){
		valuesPostfix = value;
	}
	
	public String getValuesPostfix(){
		return valuesPostfix;
	}	
	
	public void setNamePostfix(String value){
		namePostfix = value;
	}
	
	public String getNamePostfix(){
		return namePostfix;
	}		

	public void setHeadRowPrefix(String value){
		headRowPrefix = value;
	}
	
	public String getHeadRowPrefix(){
		return headRowPrefix;
	}	
	
	public void setOddRowPrefix(String value){
		oddRowPrefix = value;
	}
	
	public String getOddRowPrefix(){
		return oddRowPrefix;
	}	
	
	public void setEvenRowPrefix(String value){
		evenRowPrefix = value;
	}
	
	public String getEvenRowPrefix(){
		return evenRowPrefix;
	}
	
	public void setRowPostfix(String value){
		rowPostfix = value;
	}
	
	public String getRowPostfix(){
		return rowPostfix;
	}	
	
	public void setEmptyCellText(String value){
		emptyCellText = value;
	}
	
	public String getEmptyCellText(){
		return emptyCellText;
	}		
	
	public void setSnapshotPrefix(String value){
		snapshotPrefix = value;
	}
	
	public String getSnapshotPrefix(){
		return snapshotPrefix;
	}
	
	public void setSnapshotPostfix(String value){
		snapshotPostfix = value;
	}
	
	public String getSnapshotPostfix(){
		return snapshotPostfix;
	}	
	
	public void escapeAndWriteCellValue(StringBuffer sb, String value){
		int i;
        for (i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            
            switch (ch) {
            	case '\n' : sb.append("<br>"); break;
            	case '\r' : sb.append("\r"); break;
            	case ' ' : sb.append(" "); break;
            	case '"' : sb.append("&quot;"); break;
            	case '&' : sb.append("&amp;"); break;
            	case '<' : sb.append("&lt;"); break;            
            	case '>' : sb.append("&gt;"); break;            	
            	default :
            	    
                    // ignore all below '!'
                    if (ch < '!'){
                        ch = ' ';
                    }
            	                	    
        		    if (
        		            ('a' <= ch && ch <= 'z')
        		            ||
        		            ('A' <= ch && ch <= 'Z')
        		            ||
        		            ('0' <= ch && ch <= '9')
		            ){
        		        sb.append(ch);
            		} else {
            			sb.append("&#x" + Integer.toHexString(ch) + ";");
            		}            			
            }
        }
	}	
	
}
