package util.csv

import org.example.util.csv.parser.CSVParserRemindo
import org.example.util.csv.parser.Submission
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.apache.commons.io.IOUtils

class CSVParserRemindoTest {
    @Test
    fun veryBasicTest() {
        val input = IOUtils.toInputStream("""Result ID,Candidate ID,Section title,Question number,Question ID,Question version,Question code,Question type,Variable type,Interaction number,Variable name,Variable interpretation,Variable min. value,Variable max. value,Variable type,Variable cardinality,Variable default value,Variable value,Choice sequence
890929,144437,,1,74029,13,"ics-java-test3-entry-part1-coding-01 [2021.1, 2023.1]",extended_text,response,1,RESPONSE,,,,string,single,,"package JAVA;

public class Entry {

    public static int countDigits(int number){
        int result = 0;
        
        while(number != 0){
            if ( number % 10) {
                result++;
            }
        }
        
        return result;
    }

    public static void main(String[] args) {
        System.out.println(""4765 has ""+ countDigits(4765) + ""digits"");
        //Expected: ""4765 has 4 digits""
        
        System.out.println(""48 has ""+ countDigits(48) + "" digits"");
        //Expected: ""48 has 2 digits""
    }

}
",[]
890929,144437,,2,74034,9,"ics-java-test3-entry-part1-coding-02 [2021.1, 2023.1]",extended_text,response,1,RESPONSE,,,,string,single,,"package JAVA;

import java.util.HashSet;
import java.util.Set;

public class Entry {

	public static String notString(String s) {
        Set<Integer> hasNot = new HashSet<>();

        return s;
    }
        
    
    public static void main(String[] args){
        System.out.println(notString(""candy"") );
        //Expected: ""not candy""
        System.out.println(notString(""NOT sweet"") );
        //Expected: ""NOT sweet""
        System.out.println(notString(""Peper"") );
        //Expected: ""not Peper""
    }",[]
    
    """.trimIndent(), Charsets.UTF_8)
        val results : List<Submission> = CSVParserRemindo.parseSubmissions(input)
        assertEquals(2, results.size)
        assertEquals(890929, results[0].resultID)
        assertEquals("ics-java-test3-entry-part1-coding-01 [2021.1, 2023.1]", results[0].questionCode)
        assertTrue(results[0].variableValue.startsWith("package JAVA;"))
    }

    @Test
    fun testConvertsStrings() {
        val input = IOUtils.toInputStream("""Result ID,Candidate ID,Section title,Question number,Question ID,Question version,Question code,Question type,Variable type,Interaction number,Variable name,Variable interpretation,Variable min. value,Variable max. value,Variable type,Variable cardinality,Variable default value,Variable value,Choice sequence
890929,144437,,2,74034,9,"ics-java-test3-entry-part1-coding-02 [2021.1, 2023.1]",extended_text,response,1,RESPONSE,,,,string,single,,"package JAVA;

import java.util.HashSet;
import java.util.Set;

public class Entry {

	public static String notString(String s) {
        Set<Integer> hasNot = new HashSet<>();

        return s;
    }
        
    
    public static void main(String[] args){
        System.out.println(notString(""candy"") );
        //Expected: ""not candy""
        System.out.println(notString(""NOT sweet"") );
        //Expected: ""NOT sweet""
        System.out.println(notString(""Peper"") );
        //Expected: ""not Peper""
    }",[]
    
    """, Charsets.UTF_8)

        val results : List<Submission> = CSVParserRemindo.parseSubmissions(input)
        assertEquals(1, results.size)
        val submission = results[0]

        println(submission.variableValue)
        assertFalse(submission.variableValue.contains("\"\"")) // should remove double
        assertTrue(submission.variableValue.contains("\"not Peper\"")) // should still retain single quotes

    }
}