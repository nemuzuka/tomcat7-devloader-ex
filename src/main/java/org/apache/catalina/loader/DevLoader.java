package org.apache.catalina.loader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;

/**
 * @author Martin Kahr, Kalle Korhonen
 *
 * Kalle Korhonen / kaosko@apache.org: modified to work for Tomcat 7.x
 *
 */
public class DevLoader extends WebappLoader {
	private static final String info =
        "org.apache.catalina.loader.DevLoader/1.0";

	private String webClassPathFile = ".#webclasspath";
	private String tomcatPluginFile = ".tomcatplugin";
	private String devloaderConfFile = "conf/devloader.conf";
	private String referenceProjectPathConfFile = "conf/referencePath.conf";

	public DevLoader() {
		super();
	}
	public DevLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * @see org.apache.catalina.Lifecycle#start()
	 */
	public void startInternal() throws LifecycleException {
		log("Starting DevLoader");
		//setLoaderClass(DevWebappClassLoader.class.getName());

		super.startInternal();

		ClassLoader cl = super.getClassLoader();
		if (cl instanceof WebappClassLoader == false) {
			logError("Unable to install WebappClassLoader !");
			return;
		}
		WebappClassLoader devCl = (WebappClassLoader) cl;

		List webClassPathEntries = readWebClassPathEntries();
		List excludeWebClassPathEntries = readExcludeWebClassPathEntries();
		List referenceProjectPathEntries = readReferenceProjectPathEntries();
		StringBuffer classpath   = new StringBuffer();
		for (Iterator it = webClassPathEntries.iterator(); it.hasNext();) {
			String entry = (String) it.next();
			boolean result = addEntry(entry, excludeWebClassPathEntries, devCl, classpath);
			if(result == false) {
				//設定されたディレクトリ情報を元に、存在するかチェックする
				int size = referenceProjectPathEntries.size();
				for(int i = 0; i < size; i++) {
					ReferencePath reference = (ReferencePath) referenceProjectPathEntries.get(i);
					String followEntry = reference.prefix + entry + reference.suffix;
					result = addEntry(followEntry, excludeWebClassPathEntries, devCl, classpath);
					if(result) {
						break;
					}
				}
			}
		}
		/*
		try {
			devCl.loadClass("at.kase.webfaces.WebApplication");
			devCl.loadClass("at.kase.taglib.BaseTag");
			devCl.loadClass("at.kase.taglib.xhtml.XHTMLTag");
			devCl.loadClass("at.kase.common.reflect.ClassHelper");
			devCl.loadClass("javax.servlet.jsp.jstl.core.Config");
			log("ALL OKAY !");
		} catch(Exception e) {
			logError(e.toString());
		}*/
		String cp = (String)getServletContext().getAttribute(Globals.CLASS_PATH_ATTR);
		StringTokenizer tokenizer = new StringTokenizer(cp, File.pathSeparatorChar+"");
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			// only on windows
			if (token.charAt(0)=='/' && token.charAt(2)==':') token = token.substring(1);
			classpath.append(token + File.pathSeparatorChar);
		}
		//cp = classpath + cp;
		getServletContext().setAttribute(Globals.CLASS_PATH_ATTR, classpath.toString());
		log("JSPCompiler Classpath = " + classpath);

		// system class path for some framework
		String systemClassPath = System.getProperty("java.class.path");
		String originalSystemClassPath = System.getProperty("java.class.path.devloader");
		if (originalSystemClassPath == null) {
			System.setProperty("java.class.path.devloader", systemClassPath);
		} else {
			systemClassPath = originalSystemClassPath;
		}
		log("original system Classpath: " + systemClassPath);
		systemClassPath = systemClassPath + File.pathSeparatorChar + classpath.toString();
		System.setProperty("java.class.path", systemClassPath);
		log("temporary system Classpath: " + System.getProperty("java.class.path"));
	}

	private boolean addEntry(String entry, List excludeWebClassPathEntries, WebappClassLoader devCl,
			StringBuffer classpath) {
		File f = new File(entry);
		if (f.exists()) {
			if (f.isDirectory() && entry.endsWith("/")==false) f = new File(entry + "/");
			try {
				URL url = f.toURL();
				boolean excludeJar = false;
				for (Iterator iterator = excludeWebClassPathEntries.iterator(); iterator
						.hasNext();) {
					String excludeEntry = (String) iterator.next();
                    if (url.toString().matches(excludeEntry)) {
                    	excludeJar = true;
                    	break;
                    }
				}
				if (excludeJar) {
					log("skipped " + url.toString());
                    return true;
				}
				//devCl.addUrl(url);
				devCl.addRepository(url.toString());
				classpath.append(f.toString() + File.pathSeparatorChar);
				log("added " + url.toString());
			} catch (MalformedURLException e) {
				logError(entry + " invalid (MalformedURL)");
				return false;
			}
			return true;
		} else {
			logError(entry + " does not exist !");
			return false;
		}
	}

	protected void log(String msg) {
		System.out.println("[DevLoader] " + msg);
	}
	protected void logError(String msg) {
		System.err.println("[DevLoader] Error: " + msg);
	}

	protected List readWebClassPathEntries() {
		List rc = null;

		File prjDir = getProjectRootDir();
		if (prjDir == null) {
			return new ArrayList();
		}
		log("projectdir=" + prjDir.getAbsolutePath());

		// try loading tomcat plugin file
		// DON"T LOAD TOMCAT PLUGIN FILE (DOESN't HAVE FULL PATHS ANYMORE)
		//rc = loadTomcatPluginFile(prjDir);

		if (rc ==null) {
			rc = loadWebClassPathFile(prjDir);
		}

		if (rc == null) rc = new ArrayList(); // should not happen !
		return rc;
	}

	protected File getProjectRootDir() {
		File rootDir = getWebappDir();
		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				return (file.getName().equalsIgnoreCase(webClassPathFile) ||
				        file.getName().equalsIgnoreCase(tomcatPluginFile));
			}
		};
		while(rootDir != null) {
			File[] files = rootDir.listFiles(filter);
			if (files != null && files.length >= 1) {
				return files[0].getParentFile();
			}
			rootDir = rootDir.getParentFile();
		}
		return null;
	}

	protected List loadWebClassPathFile(File prjDir) {
		File cpFile = new File(prjDir, webClassPathFile);
		if (cpFile.exists()) {
			FileReader reader = null;
			try {
				List rc = new ArrayList();
				reader = new FileReader(cpFile);
				LineNumberReader lr = new LineNumberReader(reader);
				String line = null;
				while((line = lr.readLine()) != null) {
					// convert '\' to '/'
					line = line.replace('\\', '/');
					rc.add(line);
				}
				return rc;
			} catch(IOException ioEx) {
				if (reader != null) try { reader.close(); } catch(Exception ignored) {}
				return null;
			}
		} else {
			return null;
		}
	}

/*
	protected List loadTomcatPluginFile(File prjDir) {
		File cpFile = new File(prjDir, tomcatPluginFile);
		if (cpFile.exists()) {
			FileReader reader = null;
			try {
				StringBuffer buf = new StringBuffer();

				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cpFile)));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					buf.append(inputLine);
					buf.append('\n');
				}
				WebClassPathEntries entries = WebClassPathEntries.xmlUnmarshal(buf.toString());
				if (entries == null) {
					log("no entries found in tomcatplugin file !");
					return null;
				}
				return entries.getList();
			} catch(IOException ioEx) {
				if (reader != null) try { reader.close(); } catch(Exception ignored) {}
				return null;
			}
		} else {
			return null;
		}
	}
*/


	protected List readExcludeWebClassPathEntries() {
		String catalinaBase = System.getProperty("catalina.base");
		File confFile = new File(catalinaBase, devloaderConfFile);
		List re = new ArrayList();
		if (! confFile.exists()) {
			log("notice: " + confFile.toString() + " is not found.");
			log("notice: use default settings");
			// Tomcat
			re.add("(.*)/servlet-api(.*).jar");
			re.add("(.*)/jsp-api(.*).jar");
			// Geronimo
			re.add("(.*)/geronimo-jsp(.*).jar");
			re.add("(.*)/geronimo-servlet(.*).jar");
		} else {
			FileReader reader = null;
			try {
				reader = new FileReader(confFile);
				LineNumberReader lr = new LineNumberReader(reader);
				String line = null;
				while((line = lr.readLine()) != null) {
					// convert '\' to '/'
					line = line.replace('\\', '/');
					if (line.length() > 0 && line.charAt(0) != '#') {
						re.add(line);
					}
				}
			} catch(IOException ioEx) {
				if (reader != null) try { reader.close(); } catch(Exception ignored) {}
				return null;
			}
		}
		return re;
	}

	private List readReferenceProjectPathEntries() {

		String catalinaBase = System.getProperty("catalina.base");
		File confFile = new File(catalinaBase, referenceProjectPathConfFile);

		List re = new ArrayList();
		if (confFile.exists()) {
			FileReader reader = null;
			try {
				reader = new FileReader(confFile);
				LineNumberReader lr = new LineNumberReader(reader);
				String line = null;
				while((line = lr.readLine()) != null) {
					// convert '\' to '/'
					line = line.replace('\\', '/');
					if (line.length() > 0 && line.charAt(0) != '#') {

						String[] datas = line.split(",");
						ReferencePath target = new ReferencePath();
						if(datas.length == 1) {
							target.prefix = datas[0];
							target.suffix= "";
						} else if(datas.length >= 2) {
							target.prefix = datas[0];
							target.suffix= datas[1];
						}
						re.add(target);
					}
				}
			} catch(IOException ioEx) {
				return null;
			} finally {
				if (reader != null) try { reader.close(); } catch(Exception ignored) {}
			}
		}
		return re;
	}

	protected ServletContext getServletContext() {
		return ((Context) getContainer()).getServletContext();
	}

	protected File getWebappDir() {
		File webAppDir = new File(getServletContext().getRealPath("/"));
		return webAppDir;
	}

	class ReferencePath {
		public String prefix;
		public String suffix;
	}
}
