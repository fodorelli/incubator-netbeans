/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/**
 *
 * @author  Sanjay Dhamankar
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openide.util.NbBundle;

import org.openide.text.CloneableEditorSupport;

// public class QueryBuilderSqlTextArea extends JTextPane
public class QueryBuilderSqlTextArea extends JEditorPane
        implements ActionListener,  KeyListener {
    
    // TODO JFB:  sntax checking.   turned off, turn on when fixed.
    public static final boolean SYNTAX_HIGHLIGHT = true ;
    
    String                              _lastGoodQuery = null;


    // Private Variables
    
    private boolean                     DEBUG = false;
    private QueryBuilder                _queryBuilder;
    private JMenuItem                    runQueryMenuItem;
    private JMenuItem                    parseQueryMenuItem;
    
    // This flag is used to decide whether the focusLost is caused when the 
    // popup(Parse Query, Run Query) is displayed. In the case where the 
    // focusLost is generated by the popup menu we do not need to parse the 
    // query. This is added to fix the problem of it is no longer possible to
    // execute an arbitrary query.  That is, if the user types in a query
    // and then tries to run it, the query editor will always parse/generate
    // it, which may transform the query formulation.  That makes it harder
    // to test out minor re-formulations in syntax.
    // After removing the focus listener this may not be required
    // please remove later.
    // private boolean                     _maybeShowPopup = false;

    // private JPopupMenu _tableColumnPopup;

    // code for syntax highlighting

//    private String              keywordString        = "";
//    private int                 currentPosition         = 0;
    private SimpleAttributeSet  keyword  = new SimpleAttributeSet();
    private SimpleAttributeSet  schema   = new SimpleAttributeSet();
//    private SimpleAttributeSet  table    = new SimpleAttributeSet();
    private SimpleAttributeSet  column   = new SimpleAttributeSet();
//    private SimpleAttributeSet  normal   = new SimpleAttributeSet();

//     private DefaultStyledDocument dsDocument   =  (DefaultStyledDocument) getStyledDocument();

    private static ArrayList   keywords        = null;
    // SQL 92 keywords
    // http://sqlzoo.net/sql92.html
    private static final String[] sqlReservedWords           = new String[] {
        "ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER",         // NOI18N
        "AND", "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT",             // NOI18N
        "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIT",               // NOI18N
        "BIT_LENGTH", "BOTH", "BY", "CASCADE", "CASCADED", "CASE",       // NOI18N
        "CAST", "CATALOG", "CHAR", "CHARACTER", "CHAR_LENGTH",           // NOI18N
        "CHARACTER_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE",     // NOI18N
        "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION",        // NOI18N
        "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT",              // NOI18N
        "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT",          // NOI18N
        "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",             // NOI18N
        "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", "DEC",    // NOI18N
        "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED",       // NOI18N
        "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS",       // NOI18N
        "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "ELSE",    // NOI18N
        "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC",      // NOI18N
        "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH",    // NOI18N
        "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL",     // NOI18N
        "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING",       // NOI18N
        "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR",              // NOI18N
        "INITIALLY", "INNER", "INPUT", "INSENSITIVE", "INSERT", "INT",   // NOI18N
        "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION",   // NOI18N
        "JOIN", "KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL",   // NOI18N
        "LIKE", "LOCAL", "LOWER", "MATCH", "MAX", "MIN", "MINUTE",       // NOI18N
        "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR",      // NOI18N
        "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC",                // NOI18N
        "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR",      // NOI18N
        "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL",        // NOI18N
        "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY",       // NOI18N
        "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL",    // NOI18N
        "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT",         // NOI18N
        "ROLLBACK", "ROWS", "SCHEMA", "SCROLL", "SECOND", "SECTION",     // NOI18N
        "SELECT", "SESSION", "SESSION_USER", "SET", "SIZE", "SMALLINT",  // NOI18N
        "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE",       // NOI18N
        "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY",         // NOI18N
        "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR",                    // NOI18N
        "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION",              // NOI18N
        "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "UNION", "UNIQUE",   // NOI18N
        "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", // NOI18N
        "VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER",      // NOI18N
        "WHERE", "WITH", "WORK", "WRITE", "YEAR", "ZONE"                 // NOI18N
    };
    static {
        keywords = new ArrayList();
        for (int i=0; i<sqlReservedWords.length; i++) {
            keywords.add(sqlReservedWords[i]);
        }
    }
    
    // Constructor
    
    public QueryBuilderSqlTextArea(QueryBuilder queryBuilder) {
        super();
        _queryBuilder = queryBuilder;
        createSqlTextPopup();
        // Get Netbeans-registered EditorKit for SQL content
	setEditorKit(CloneableEditorSupport.getEditorKit("text/x-sql"));
        if ( SYNTAX_HIGHLIGHT ) {
            addKeyListener(this);
        }
        
        // set the bold attribute
        // colors chosen from :
        // http://ui.netbeans.org/docs/hi/annotations/index2.html
        StyleConstants.setForeground(keyword,new Color(0,0,153));
        StyleConstants.setForeground(schema, new Color(0,111,0));
        StyleConstants.setForeground(column,new Color(120,0,0));
          
        // Add support for code completion (comment out, breaks syntax highlighting)
//        QueryBuilderSqlCompletion doc = new QueryBuilderSqlCompletion( this, sqlReservedWords);
//        this.setDocument(doc);
    }
    
    
    
    public void keyTyped(KeyEvent e) {
//         if ( SYNTAX_HIGHLIGHT
//                 // we don't recognize the syntax, no highlighting.
//                 && (_queryBuilder.getParseErrorMessage() == null ) ) {
//             this.currentPosition         = this.getCaretPosition();
//             processChar(e.getKeyChar());                      
//         }
    }
    
    /** ignore */
    public void keyReleased(KeyEvent e) {
    }
    
    /** Handle the key pressed event and change the focus if a particular
     * key combination is pressed. */
    public void keyPressed(KeyEvent e) {
//         if( e.isShiftDown() ) {
//             int code = e.getKeyCode();
//             switch(code) {
//                 // diagram pane
//                 case KeyEvent.VK_F10:
//                     // this check is added to fix a bug where the popup menu
//                     // remains disabled when the user has removed the last table
//                     // from the graph and then added (typed/pasted) text query.
//                     // First check if the text is not just white spaces.
//                     if ( ((JTextPane)(e.getComponent())).getText().trim().length() != 0 ) {
//                         parseQueryMenuItem.setEnabled(true);
//                         runQueryMenuItem.setEnabled(true);
//                     } else {
//                         // user may have just typed white spaces or may have typed
//                         // a wrong query which got restored after he 'cancel'ed
//                         // the warning dialog about non-standard query, which
//                         // restored the previous 'blank' query.
//                         parseQueryMenuItem.setEnabled(false);
//                         runQueryMenuItem.setEnabled(false);
//                     }
//                     _maybeShowPopup = true;
//                     _sqlTextPopup.show(e.getComponent(), 0, 0);
//                     break;
//             }
//         }
//         _queryBuilder.handleKeyPress(e);
    }
    
    // code related to syntax highlighting
    
//     // Replace the document string at the given position  ( pos )
//     // with the string ( str ) and with the given attributes ( attr )
//     private void replaceString( String str, int pos,
//             SimpleAttributeSet attr ) {
//         try {
//             dsDocument.remove( pos - str.length(), str.length() );
//             dsDocument.insertString(pos - str.length(), str, attr);
//         } catch (Exception ex){
//             // should never happen !!!
//             // ex.printStackTrace();
//         }
//     }
    
//     // function which checks if the current paragraph element is
//     // either a SQL keyword, a schema name , a table name or a column name.
//     private void checkKeyword() {
//         int offset = this.currentPosition;
//         Element element = dsDocument.getParagraphElement( offset );
//         String elementText = "";
//         try {
//             elementText = dsDocument.getText( element.getStartOffset(),
//                     element.getEndOffset() - element.getStartOffset() );
//         } catch ( Exception ex ) {
//             // should never happen !!!
//             // ex.printStackTrace();
//         }
//         int elementTextLength = elementText.length();
//         if ( elementTextLength == 0 ) return;
        
//         int i = 0;
        
//         if ( element.getStartOffset() > 0 ) {
//             offset = offset - element.getStartOffset();
//         }
//         if ( ( offset >= 0 ) && ( offset <= elementTextLength-1 ) ) {
//             i = offset;
//             while ( i > 0 ){
//                 // traverse back until a delimiter is found
//                 i--;
//                 char charAt = elementText.charAt( i );
//                 if ( (charAt == ' ') || (i == 0) ||             // NOI18N
//                         (charAt == '.') || (charAt == '"') ||      // NOI18N
//                         (charAt == '\'') ||      // NOI18N
//                         (charAt == '\t') ||      // NOI18N
//                         (charAt == ',') ) {      // NOI18N
//                     if (i != 0) {
//                         i++;
//                     }
//                     keywordString =
//                             elementText.substring(i, offset);//skip the period
                    
//                     String s = keywordString.trim().toUpperCase();
//                     String db_element = keywordString.trim();
//                     // check if it is a keyword
//                     if (keywords.contains(s)){
//                         replaceString(s, currentPosition, keyword);
//                     }
//                     // check if it is schema name
//                     else if ( _queryBuilder.isSchemaName( db_element ) ) {
//                         replaceString(db_element, currentPosition, schema);
//                     }
//                     // check if it is table name
//                     else if ( _queryBuilder.isTableName( db_element ) ) {
//                         replaceString(db_element, currentPosition, table);
//                     }
//                     // check if it is column name
//                     else if ( _queryBuilder.isColumnName( db_element ) ) {
//                         replaceString(db_element, currentPosition, column);
//                     }
//                     // if none of the above is true, insert the text string
//                     // with normal attributes.
//                     else {
//                         replaceString( keywordString, currentPosition, normal);
//                     }
//                     break;
//                 }
//             }
//         }
//     }
    
    
//     // function which checks if the current paragraph element is
//     // either a SQL keyword, a schema name , a table name or a column name.
//     private void checkString() {
//         int offset = this.currentPosition;
//         Element element = dsDocument.getParagraphElement( offset );
//         String elementText = "";
//         try {
//             elementText = dsDocument.getText( element.getStartOffset(),
//                     element.getEndOffset() - element.getStartOffset() );
//         } catch ( Exception ex ) {
//             // should never happen !!!
//             // ex.printStackTrace();
//         }
//         int elementTextLength = elementText.length();
//         if ( elementTextLength == 0 ) return;
        
//         int i = 0;
        
//         if ( element.getStartOffset() > 0 ) {
//             offset = offset - element.getStartOffset();
//         }
//         if ( ( offset >= 0 ) && ( offset <= elementTextLength ) ) {
//             i = offset;
//             while ( i > 0 ){
//                 // traverse back until a delimiter is found
//                 i--;
//                 char charAt = elementText.charAt( i );
//                 if ( (charAt == '"') || (charAt == '\'' ) ) {      // NOI18N
//                     keywordString =
//                             elementText.substring(i, offset);//skip the period
                    
//                     String s = keywordString.toUpperCase();
//                     String db_element = keywordString;
//                     String db_element_wo_quotes;
//                     if ( keywordString.length() > 2 &&
//                             ( (keywordString.startsWith("\"") &&  keywordString.endsWith("\"") ) ||
//                             (keywordString.startsWith("\'") &&  keywordString.endsWith("\'") ) ) ) {
//                         db_element_wo_quotes =
//                                 keywordString.substring(1, keywordString.length()-1);
//                     } else if (keywordString.length() > 2 && ( keywordString.startsWith("\"") || keywordString.startsWith("\'") ) ) {
//                         db_element_wo_quotes =
//                                 keywordString.substring(1, keywordString.length());
//                     } else
//                         db_element_wo_quotes = keywordString;
//                     // check if it is schema name
//                     if ( _queryBuilder.isSchemaName( db_element_wo_quotes ) ) {
//                         replaceString(db_element, currentPosition, schema);
//                     }
//                     // check if it is table name
//                     else if ( _queryBuilder.isTableName( db_element_wo_quotes ) ) {
//                         replaceString(db_element, currentPosition, table);
//                     }
//                     // check if it is column name
//                     else if ( _queryBuilder.isColumnName( db_element_wo_quotes ) ) {
//                         replaceString(db_element, currentPosition, column);
//                     }
//                     // if none of the above is true, insert the text string
//                     // with normal attributes.
//                     else {
//                         replaceString( keywordString, currentPosition, normal);
//                     }
//                     break;
//                 }
//             }
//         }
//     }
    
    
//     private void processString( String str ) {
//         char strChar = str.charAt(0);
//         // if '"' is encontered keep processing till the next one is found
//         if ( strChar ==  '"'  || strChar == '\'') { // NOI18N
//             if ( stringIsParsed ) {
//                 checkString();
//                 stringIsParsed = false;
//             } else
//                 stringIsParsed = true;
//         }
//         if ( ! stringIsParsed ) {
//             // if a white-space character or a '.' or ',' is encountered
//             // check if it is a keyword
//             if ( strChar ==  ' '  || strChar == '\n' ||               // NOI18N
//                     strChar == '\t' || strChar ==  '.'  ||               // NOI18N
//                     strChar == ',' ) {                                   // NOI18N
//                 checkKeyword();
//             }
//         } else {
//             checkString();
//         }
//     }
    
//     private void processChar(char strChar) {
//         char[] chrstr = new char[1];
//         chrstr[0] = strChar;
//         String str = new String(chrstr);
//         //this.keysTyped = str;
//         processString(str);
//     }
    
//     private void processWords(String str){
//         StringBuffer wordBuffer = new StringBuffer();;
//         stringIsParsed = false;
//         for ( int i =0; i < str.length(); i++ ) {
//             char strChar = str.charAt(i);
//             if ( strChar ==  '"'  || strChar == '\'') { // NOI18N
//                 if ( stringIsParsed ) {
//                     stringIsParsed = false;
//                     wordBuffer.append(strChar);
//                     processWord( i, wordBuffer.toString());
//                     wordBuffer = null;
//                     wordBuffer = new StringBuffer();
//                 } else {
//                     stringIsParsed = true;
//                 }
//             }
//             if (!stringIsParsed) {
//                 if ( strChar ==  ' '  || strChar == '\n' ||           // NOI18N
//                         strChar == '\t' || strChar ==  '.'  ||               // NOI18N
//                         strChar == ',' ) {                                   // NOI18N
//                     processWord( i, wordBuffer.toString());
//                     wordBuffer = null;
//                     wordBuffer = new StringBuffer();
//                 } else
//                     wordBuffer.append(strChar);
//             } else {
//                 wordBuffer.append(strChar);
//             }
//         }
//     }
    
//     private void processWord(int position, String str)  {
//         String s = str.toUpperCase();
//         String db_element = str;
//         String db_element_wo_quotes;
//         if (str.length() > 2 &&
//                 ( (str.startsWith("\"") &&  str.endsWith("\"") ) ||
//                 (str.startsWith("\'") &&  str.endsWith("\'") ) ) ) {
//             db_element_wo_quotes = str.substring(1, str.length()-1);
//             position = position+1;
//         } else if ( str.length() > 2 &&
//                 ( str.startsWith("\"")  ||
//                 str.startsWith("\'") ) )  {
//             db_element_wo_quotes = str.substring(1, str.length());
//             position = position+1;
//         } else {
//             db_element_wo_quotes = str;
//         }
        
//         boolean checkMore = true ;
//         // check if it is a keyword
//         if (keywords.contains(s)){
//             replaceString(s, position, keyword);
//             checkMore = false ;
//         }
//         // check if it is schema name
        
//         while ( checkMore ) {
//             if ( _queryBuilder.isSchemaName( db_element_wo_quotes ) ) {
//                 replaceString(db_element, position, schema);
//                 break ;
//             }
//             if ( _queryBuilder.isTableName( db_element_wo_quotes ) ) {
//                 replaceString(db_element, position, table);
//                 break ;
//             }
            
//             // check if it is column name
//             if ( _queryBuilder.isColumnName( db_element_wo_quotes ) ) {
//                 replaceString(db_element, position, column);
//                 break ;
//             }
            
            
//             replaceString( str, position, normal);
            
//             break ;
//         }
//     }
    
    // Create a background menu, with entries for parsing or executing the query.
    
    void createSqlTextPopup() {
        JPopupMenu sqlTextPopup;
        
        //Create the popup menu.
        sqlTextPopup = new JPopupMenu();
        
        parseQueryMenuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderSqlTextArea.class, "PARSE_QUERY"));      // NOI18N
        parseQueryMenuItem.addActionListener(this);
        sqlTextPopup.add(parseQueryMenuItem);
        
        runQueryMenuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderSqlTextArea.class, "RUN_QUERY")); // NOI18N
        runQueryMenuItem.addActionListener(this);
        sqlTextPopup.add(runQueryMenuItem);
        
        // Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new sqlTextListener(sqlTextPopup);
        super.addMouseListener(popupListener);
    }
    
    public void setParseQueryMenuEnabled( boolean onOff ) {
        parseQueryMenuItem.setEnabled(onOff);
    }
    
    public void setRunQueryMenuEnabled( boolean onOff ) {
        runQueryMenuItem.setEnabled(onOff);
    }
    
    
    // Inner classes, for menus
    
    class sqlTextListener extends MouseAdapter {
        
        JPopupMenu popup;
        
        sqlTextListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            mousePressed(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                // this check is added to fix a bug where the popup menu
                // remains disabled when the user has removed the last table
                // from the graph and then added (typed/pasted) text query.
                // First check if the text is not just white spaces.
                if ( ((JEditorPane)(e.getComponent())).getText().trim().length() != 0 ) {
                    parseQueryMenuItem.setEnabled(true);
                    runQueryMenuItem.setEnabled(true);
                } else {
                    // user may have just typed white spaces or may have typed
                    // a wrong query which got restored after he 'cancel'ed
                    // the warning dialog about non-standard query, which
                    // restored the previous 'blank' query.
                    parseQueryMenuItem.setEnabled(false);
                    runQueryMenuItem.setEnabled(false);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    public boolean queryChanged() {
        return ( ! this.getText().equals( _lastGoodQuery ) );
    }
    
    
    // Actions -- Menu selections
    
    public void actionPerformed(ActionEvent e) {
        
        JMenuItem source = (JMenuItem)(e.getSource());
        
        if (source.getText().equals(NbBundle.getMessage(QueryBuilder.class, "PARSE_QUERY"))) {    // NOI18N
            
            String currentQuery = this.getText();
            if ( (currentQuery != null) &&
                 (currentQuery.trim().length() != 0 ) ) 
            {
                QueryBuilder.showBusyCursor( true );
                _queryBuilder.populate(currentQuery, true);
                QueryBuilder.showBusyCursor( false );
            }
        } else if (source.getText().equals(NbBundle.getMessage(QueryBuilder.class, "RUN_QUERY"))) { // NOI18N
            
            String currentQuery = this.getText();

            // if query is changed then parse it first
            if ( (currentQuery != null) &&
                 (currentQuery.trim().length() != 0 ) ) 
            {
                // if query matches last good one, no need to parse
                if ( ! (currentQuery.trim().equals( _lastGoodQuery )) ) {

                    QueryBuilder.showBusyCursor( true );
                    // run the query even if there is a parse error
                    // as this may be query not correctly parsed by the parser
                    // but still it may be a valid query which the user
                    // may want to run.  See 6254361
                    _queryBuilder.populate(this.getText(), true) ;
                    QueryBuilder.showBusyCursor( false );
                }
            } else {
                return;
            }
            
            // Execute the query
            _queryBuilder.executeQuery(this.getText());
            
        }
    }
    
    /**
     * Sets the text of this TextComponent  to the specified text. Also records it
     * for possible reset later.
     */
    public void setText(String str) {
        
         if (DEBUG)
            System.out.println("setQueryText called with " + str + "\n" ); // NOI18N

        // To make sure the last part of the incoming string is highlighted.
        // String text = new String( str + " " );
        String text = str.trim();
        super.setText(text);
        
        /*
        if (DEBUG)  {
          PerfTimer perfTimer = new PerfTimer();
          perfTimer.print("setQueryText: Before processWords : ");    // NOI18N
        }
         */
        /*
         if ( SYNTAX_HIGHLIGHT
                 // we don't recognize the syntax, no highlighting.
                 && (_queryBuilder.getParseErrorMessage() == null ) ) {
             processWords(text);
         }
         */
        /*
        if (DEBUG)
          perfTimer.print("setQueryText: After processWords : ");     // NOI18N
         */
        if (  ! text.equals( _lastGoodQuery ) ) {
            _lastGoodQuery = text;   
        }
    }
    
/*
   public class PerfTimer {
 
        long _time;
 
        public PerfTimer() {
            resetTimer();
        }
 
        // reset Timer
        public void resetTimer(){
            // set current time
            _time = System.currentTimeMillis();
        }
 
        public long elapsedTime() {
            // get elapsed Time
            return (System.currentTimeMillis() - _time);
        }
 
        public void print(String aString) {
            System.out.println(aString + ": " + this.elapsedTime() + " ms"); // NOI18N
        }
    }
 */
    
    /**
     *  Restore the last good query.
     */
    
    public void restoreLastGoodQuery() {
        super.setText( _lastGoodQuery );
    }
    
    /**
     *  Save the last good query.
     */
    
    public void saveLastGoodQuery() {
        _lastGoodQuery = this.getText() ;
    }
    
    /**
     * Clears the text area
     */
    void clear() {
        this.setText(""); // NOI18N
    }
    
}