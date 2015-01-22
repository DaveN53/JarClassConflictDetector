package com.attivio.maven.util;

import java.util.ArrayList;
import java.util.List;

public class Ignores 
{
	private List<Ignore> ignores = new ArrayList<Ignore>();
    public void add( Ignore ign ){ this.ignores.add(ign); }
    
    public List<Ignore> getIgnores() { return ignores; }
    public void setIgnores(List<Ignore> ignores) { this.ignores = ignores; }
}
