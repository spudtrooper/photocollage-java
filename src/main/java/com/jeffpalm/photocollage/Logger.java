package com.jeffpalm.photocollage;

import java.util.HashMap;
import java.util.Map;

public class Logger {
  
  private final static Map<String, Logger> loggerMap = new HashMap<String, Logger>();
  
  private final String name;
  
  private Logger(String name) {
    this.name = name;
  }
  
  public void info(String msg) {
    System.err.println(name + " " + msg);
  }

  public void infof(String tmpl, Object... args) {
    System.err.println(String.format(name + " " + tmpl, args));
  }


  public static Logger getLogger(String name) {
    Logger logger = loggerMap.get(name);
    if (logger == null) {
      logger = new Logger(name);
      loggerMap.put(name, logger);
    }
    return logger;
  }
}
