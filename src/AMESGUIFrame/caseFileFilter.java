

package AMESGUIFrame;
/*
 * caseFileFilter.java
 *
 * Created on 2007 1 28, 10:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



import java.io.File;
import javax.swing.filechooser.FileFilter;

 class caseFileFilter extends FileFilter {
 
  String[] extensions;

  String description;

  public caseFileFilter(String ext) {
    this(new String[] { ext }, null);
  }

  public caseFileFilter(String[] exts, String descr) {
    extensions = new String[exts.length];
    for (int i = exts.length - 1; i >= 0; i--) {
      extensions[i] = exts[i].toLowerCase();
    }
     
    description = (descr == null ? exts[0] + " files" : descr);
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String name = f.getName().toLowerCase();
    for (int i = extensions.length - 1; i >= 0; i--) {
      if (name.endsWith(extensions[i])) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return description;
  }
    
 }
