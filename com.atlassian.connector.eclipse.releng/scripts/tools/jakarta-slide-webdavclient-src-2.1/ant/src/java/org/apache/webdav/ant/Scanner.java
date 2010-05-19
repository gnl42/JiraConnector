/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/Scanner.java,v 1.3 2004/07/28 09:31:49 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:31:49 $
 * ========================================================================
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */
package org.apache.webdav.ant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * Class for scanning a directory for files/directories that match a certain
 * criteria.
 * <p>
 * These criteria consist of a set of include and exclude patterns. With these
 * patterns, you can select which files you want to have included, and which
 * files you want to have excluded.
 * <p>
 * The idea is simple. A given directory is recursively scanned for all files
 * and directories. Each file/directory is matched against a set of include
 * and exclude patterns. Only files/directories that match at least one
 * pattern of the include pattern list, and don't match a pattern of the
 * exclude pattern list will be placed in the list of files/directories found.
 * <p>
 * When no list of include patterns is supplied, "**" will be used, which
 * means that everything will be matched. When no list of exclude patterns is
 * supplied, an empty list is used, such that nothing will be excluded.
 * <p>
 * The pattern matching is done as follows:
 * The name to be matched is split up in path segments. A path segment is the
 * name of a directory or file, which is bounded by
 * <code>File.separator</code> ('/' under UNIX, '\' under Windows).
 * E.g. "abc/def/ghi/xyz.java" is split up in the segments "abc", "def", "ghi"
 * and "xyz.java".
 * The same is done for the pattern against which should be matched.
 * <p>
 * Then the segments of the name and the pattern will be matched against each
 * other. When '**' is used for a path segment in the pattern, then it matches
 * zero or more path segments of the name.
 * <p>
 * There are special case regarding the use of <code>File.separator</code>s at
 * the beginningof the pattern and the string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string
 * to match must also start with a <code>File.separator</code>.
 * When a pattern does not start with a <code>File.separator</code>, the
 * string to match may not start with a <code>File.separator</code>.
 * When one of these rules is not obeyed, the string will not
 * match.
 * <p>
 * When a name path segment is matched against a pattern path segment, the
 * following special characters can be used:
 * '*' matches zero or more characters,
 * '?' matches one character.
 * <p>
 * Examples:
 * <p>
 * "**\*.class" matches all .class files/dirs in a directory tree.
 * <p>
 * "test\a??.java" matches all files/dirs which start with an 'a', then two
 * more characters and then ".java", in a directory called test.
 * <p>
 * "**" matches everything in a directory tree.
 * <p>
 * "**\test\**\XYZ*" matches all files/dirs that start with "XYZ" and where
 * there is a parent directory called test (e.g. "abc\test\def\ghi\XYZ123").
 * <p>
 * Example of usage:
 * <pre>
 *   String[] includes = {"**\\*.class"};
 *   String[] excludes = {"modules\\*\\**"};
 *   ds.setIncludes(includes);
 *   ds.setExcludes(excludes);
 *   ds.setBasedir(new File("test"));
 *   ds.scan();
 *
 *   System.out.println("FILES:");
 *   String[] files = ds.getIncludedFiles();
 *   for (int i = 0; i < files.length;i++) {
 *     System.out.println(files[i]);
 *   }
 * </pre>
 * This will scan a directory called test for .class files, but excludes all
 * .class files in all directories under a directory called "modules"
 *
 */
public abstract class Scanner {
   
   protected static final String SEPARATOR = "/";
   protected static final char SEPARATOR_CHAR = '/';

//    /**
//     * Patterns that should be excluded by default.
//     *
//     * @see #addDefaultExcludes()
//     */
//    private final static String[] DEFAULTEXCLUDES = {
//        "**/*~",
//        "**/#*#",
//        "**/%*%",
//        "**/CVS",
//        "**/CVS/*",
//        "**/.cvsignore"
//    };

    /**
     * The patterns for the files that should be included.
     */
    private List includes = new ArrayList();

    /**
     * The patterns for the files that should be excluded.
     */
    private List excludes = new ArrayList();

    /**
     * The files that where found and matched at least one includes, and matched
     * no excludes.
     */
    protected List filesIncluded;

    /**
     * The files that where found and did not match any includes.
     */
    protected List filesNotIncluded;

    /**
     * The files that where found and matched at least one includes, and also
     * matched at least one excludes.
     */
    protected List filesExcluded;

    /**
     * The directories that where found and matched at least one includes, and
     * matched no excludes.
     */
    protected List dirsIncluded;

    /**
     * The directories that where found and did not match any includes.
     */
    protected List dirsNotIncluded;

    /**
     * The files that where found and matched at least one includes, and also
     * matched at least one excludes.
     */
    protected List dirsExcluded;
    
    protected boolean isCaseSensitive = true; 



    /**
     * Constructor.
     */
    public Scanner() {
    }



    /**
     * Sets the set of include patterns to use. All '/' and '\' characters are
     * replaced by <code>File.separatorChar</code>. So the separator used need
     * not match <code>File.separatorChar</code>.
     * <p>
     * When a pattern ends with a '/' or '\', "**" is appended.
     *
     * @param includes list of include patterns
     */
    public void setIncludes(String[] includes) {
       this.includes = new ArrayList();
       addIncludes(includes);
    }
    public void addIncludes(String[] includes) {
       if (includes != null) {
          for(int i = 0; i < includes.length ; i++) {
             this.includes.add(noramlizePattern(includes[i]));
          }
       }
    }

    /**
     * Sets the set of exclude patterns to use. All '/' and '\' characters are
     * replaced by <code>File.separatorChar</code>. So the separator used need
     * not match <code>File.separatorChar</code>.
     * <p>
     * When a pattern ends with a '/' or '\', "**" is appended.
     *
     * @param excludes list of exclude patterns (a list of Strings)
     */
    public void setExcludes(String[] excludes) {
       this.excludes = new ArrayList();
       addExcludes(excludes);
    }
    public void addExcludes(String[] excludes) {
       if (excludes != null) {
          for (int i = 0; i < excludes.length; i++) {
             this.excludes.add(noramlizePattern(excludes[i]));
          }
       }
    }
    
    private String noramlizePattern(String pattern) {
//      pattern = pattern.replace('/',getSeparatorChar()).
//                        replace('\\',getSeparatorChar());
      if (pattern.endsWith(SEPARATOR)) {
         pattern += "**";
      }
      return pattern;
    }


    public void setCaseSensitive(boolean val) {
       this.isCaseSensitive = val;
    }


    /**
     * Scans the base directory for files that match at least one include
     * pattern, and don't match any exclude patterns.
     *
     * @exception IllegalStateException when basedir was set incorrecly
     */
    public abstract void scan();



    /**
     * Tests whether a name matches against at least one include pattern.
     *
     * @param name the name to match
     * @return <code>true</code> when the name matches against at least one
     *         include pattern, <code>false</code> otherwise.
     */
    protected boolean isIncluded(String name) {
        for (Iterator i = this.includes.iterator(); i.hasNext();) {
            if (matchPath((String)i.next(), name, this.isCaseSensitive)) {
                return true;
            }
        }
        return false;
    }



    /**
     * Tests whether a name matches against at least one exclude pattern.
     *
     * @param name the name to match
     * @return <code>true</code> when the name matches against at least one
     *         exclude pattern, <code>false</code> otherwise.
     */
    protected boolean isExcluded(String name) {
        for (Iterator i = this.excludes.iterator(); i.hasNext();) {
           if (matchPath((String)i.next(), name, this.isCaseSensitive)) {
              return true;
           }
        }
        return false;
    }



    /**
     * Get the names of the files that matched at least one of the include
     * patterns, an matched none of the exclude patterns.
     * The names are relative to the basedir.
     *
     * @return the names of the files
     */
    public String[] getIncludedFiles() {
        int count = filesIncluded.size();
        String[] files = new String[count];
        for (int i = 0; i < count; i++) {
            files[i] = (String)filesIncluded.get(i);
        }
        return files;
    }



    /**
     * Get the names of the files that matched at none of the include patterns.
     * The names are relative to the basedir.
     *
     * @return the names of the files
     */
    public String[] getNotIncludedFiles() {
        int count = filesNotIncluded.size();
        String[] files = new String[count];
        for (int i = 0; i < count; i++) {
            files[i] = (String)filesNotIncluded.get(i);
        }
        return files;
    }



    /**
     * Get the names of the files that matched at least one of the include
     * patterns, an matched also at least one of the exclude patterns.
     * The names are relative to the basedir.
     *
     * @return the names of the files
     */
    public String[] getExcludedFiles() {
        int count = filesExcluded.size();
        String[] files = new String[count];
        for (int i = 0; i < count; i++) {
            files[i] = (String)filesExcluded.get(i);
        }
        return files;
    }



    /**
     * Get the names of the directories that matched at least one of the include
     * patterns, an matched none of the exclude patterns.
     * The names are relative to the basedir.
     *
     * @return the names of the directories
     */
    public String[] getIncludedDirectories() {
        int count = dirsIncluded.size();
        String[] directories = new String[count];
        for (int i = 0; i < count; i++) {
            directories[i] = (String)dirsIncluded.get(i);
        }
        return directories;
    }



    /**
     * Get the names of the directories that matched at none of the include
     * patterns.
     * The names are relative to the basedir.
     *
     * @return the names of the directories
     */
    public String[] getNotIncludedDirectories() {
        int count = dirsNotIncluded.size();
        String[] directories = new String[count];
        for (int i = 0; i < count; i++) {
            directories[i] = (String)dirsNotIncluded.get(i);
        }
        return directories;
    }



    /**
     * Get the names of the directories that matched at least one of the include
     * patterns, an matched also at least one of the exclude patterns.
     * The names are relative to the basedir.
     *
     * @return the names of the directories
     */
    public String[] getExcludedDirectories() {
        int count = dirsExcluded.size();
        String[] directories = new String[count];
        for (int i = 0; i < count; i++) {
            directories[i] = (String)dirsExcluded.get(i);
        }
        return directories;
    }



//    /**
//     * Adds the array with default exclusions to the current exclusions set.
//     *
//     */
//    public void addDefaultExcludes() {
//       if (this.excludes == null) {
//          this.excludes = new ArrayList();
//       }
//        excludes.addAll(Arrays.asList(DEFAULTEXCLUDES));
//    }

//    protected abstract String getSeparator();
//
//    protected char getSeparatorChar() {
//        return ((getSeparator().length() > 0) ? getSeparator().charAt(0) : '/');
//    }


    /**
     * Tests whether or not a string matches against a pattern.
     * The pattern may contain two special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against.
     *                Must not be <code>null</code>.
     * @param str     The string which must be matched against the pattern.
     *                Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed
     *                        case sensitively.
     * @see SelectorUtils#match(java.lang.String, java.lang.String, boolean)
     *
     * @return <code>true</code> if the string matches against the pattern,
     *         or <code>false</code> otherwise.
     */
    public static boolean match(String pattern, String str,
                                boolean isCaseSensitive) 
    {
       return SelectorUtils.match(pattern, str, isCaseSensitive);
    }

   /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against. Must not be
     *                <code>null</code>.
     * @param str     The path to match, as a String. Must not be
     *                <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed
     *                        case sensitively.
     *
     * @return <code>true</code> if the pattern matches against the string,
     *         or <code>false</code> otherwise.
     * @see SelectorUtils#matchPath(java.lang.String, java.lang.String, boolean)
     *      (but this uses always File.Selector)
     */
    public static boolean matchPath(String pattern, String str,
                                    boolean isCaseSensitive)
    {
        // When str starts with a File.separator, pattern has to start with a
        // File.separator.
        // When pattern starts with a File.separator, str has to start with a
        // File.separator.
        if (str.startsWith(SEPARATOR) != pattern.startsWith(SEPARATOR)) {
            return false;
        }
   
        String[] patDirs = tokenizePathAsArray(pattern);
        String[] strDirs = tokenizePathAsArray(str);
   
        int patIdxStart = 0;
        int patIdxEnd = patDirs.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.length - 1;
   
        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs[patIdxStart];
            if (patDir.equals("**")) {
                break;
            }
            if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
                patDirs = null;
                strDirs = null;
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs[i].equals("**")) {
                    patDirs = null;
                    strDirs = null;
                    return false;
                }
            }
            return true;
        } else {
            if (patIdxStart > patIdxEnd) {
                // String not exhausted, but pattern is. Failure.
                patDirs = null;
                strDirs = null;
                return false;
            }
        }
   
        // up to last '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs[patIdxEnd];
            if (patDir.equals("**")) {
                break;
            }
            if (!match(patDir, strDirs[strIdxEnd], isCaseSensitive)) {
                patDirs = null;
                strDirs = null;
                return false;
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs[i].equals("**")) {
                    patDirs = null;
                    strDirs = null;
                    return false;
                }
            }
            return true;
        }
   
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patDirs[i].equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // '**/**' situation, so skip one
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
                        for (int i = 0; i <= strLength - patLength; i++) {
                            for (int j = 0; j < patLength; j++) {
                                String subPat = patDirs[patIdxStart + j + 1];
                                String subStr = strDirs[strIdxStart + i + j];
                                if (!match(subPat, subStr, isCaseSensitive)) {
                                    continue strLoop;
                                }
                            }
   
                            foundIdx = strIdxStart + i;
                            break;
                        }
   
            if (foundIdx == -1) {
                patDirs = null;
                strDirs = null;
                return false;
            }
   
            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }
   
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (!patDirs[i].equals("**")) {
                patDirs = null;
                strDirs = null;
                return false;
            }
        }
   
        return true;
    }
    
    private static String[] tokenizePathAsArray(String path) {
       char sep = SEPARATOR_CHAR;
       int start = 0;
       int len = path.length();
       int count = 0;
       for (int pos = 0; pos < len; pos++) {
           if (path.charAt(pos) == sep) {
               if (pos != start) {
                   count++;
               }
               start = pos + 1;
           }
       }
       if (len != start) {
           count++;
       }
       String[] l = new String[count];
       count = 0;
       start = 0;
       for (int pos = 0; pos < len; pos++) {
           if (path.charAt(pos) == sep) {
               if (pos != start) {
                   String tok = path.substring(start, pos);
                   l[count++] = tok;
               }
               start = pos + 1;
           }
       }
       if (len != start) {
           String tok = path.substring(start);
           l[count/*++*/] = tok;
       }
       return l;
   }
}
