package org.jenkinsci.plugins.vmanager;

public class JUnitRequestHolder {

	private boolean generateJUnitXML;
	private boolean extraAttributesForFailures; 
	private String staticAttributeList;
	
	
	
	
	public JUnitRequestHolder(boolean generateJUnitXML, boolean extraAttributesForFailures, String staticAttributeList) {
		super();
		this.generateJUnitXML = generateJUnitXML;
		this.extraAttributesForFailures = extraAttributesForFailures;
		this.staticAttributeList = staticAttributeList;
		
	}
	
	public boolean isGenerateJUnitXML() {
		return generateJUnitXML;
	}
	public void setGenerateJUnitXML(boolean generateJUnitXML) {
		this.generateJUnitXML = generateJUnitXML;
	}
	public boolean istExtraAttributesForFailures() {
		return extraAttributesForFailures;
	}
	public void setExtraAttributesForFailuresType(boolean extraAttributesForFailures) {
		this.extraAttributesForFailures = extraAttributesForFailures;
	}
	public String getStaticAttributeList() {
		return staticAttributeList;
	}
	public void setStaticAttributeList(String staticAttributeList) {
		this.staticAttributeList = staticAttributeList;
	}
	
	
}
