package com.github.difflib.text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DiffRowGeneratorTest {

    @Test
    public void testGenerator_Default() {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
    }

    /**
     * Test of normalize method, of class StringUtils.
     */
    @Test
    public void testNormalize_List() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .build();
        assertEquals(Collections.singletonList("    test"), generator.normalizeLines(Collections.singletonList("\ttest")));
    }

    @Test
    public void testGenerator_Default2() {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .columnWidth(0) // do not wrap
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
    }

    @Test
    public void testGenerator_InlineDiff() {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
        assertTrue(rows.get(0).getOldLine().indexOf("<span") > 0);
    }

    @Test
    public void testGenerator_IgnoreWhitespaces() {
        String first = "anything \n \nother\nmore lines";
        String second = "anything\n\nother\nsome more lines";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .ignoreWhiteSpaces(true)
                .columnWidth(Integer.MAX_VALUE) // do not wrap
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(4, rows.size());
        assertEquals(rows.get(0).getTag(), DiffRow.Tag.EQUAL);
        assertEquals(rows.get(1).getTag(), DiffRow.Tag.EQUAL);
        assertEquals(rows.get(2).getTag(), DiffRow.Tag.EQUAL);
        assertEquals(rows.get(3).getTag(), DiffRow.Tag.CHANGE);
    }

    private List<String> split(String content) {
        return Arrays.asList(content.split("\n"));
    }

    private void print(List<DiffRow> diffRows) {
        for (DiffRow row : diffRows) {
            System.out.println(row);
        }
    }

    @Test
    public void testGeneratorWithWordWrap() {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .columnWidth(5)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
        assertEquals("[CHANGE,anyth<br/>ing ,anyth<br/>ing]", rows.get(0).toString());
        assertEquals("[CHANGE, ,]", rows.get(1).toString());
        assertEquals("[EQUAL,other,other]", rows.get(2).toString());
    }

    @Test
    public void testGeneratorWithMerge() {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
        assertEquals("[CHANGE,anything<span class=\"editOldInline\"> </span>,anything]", rows.get(0).toString());
        assertEquals("[CHANGE,<span class=\"editOldInline\"> </span>,]", rows.get(1).toString());
        assertEquals("[EQUAL,other,other]", rows.get(2).toString());
    }

    @Test
    public void testGeneratorWithMerge2() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("Test"), Arrays.asList("ester"));
        print(rows);

        assertEquals(1, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">T</span>est<span class=\"editNewInline\">er</span>,ester]", rows.get(0).toString());
    }

    @Test
    public void testGeneratorWithMerge3() {
        String first = "test\nanything \n \nother";
        String second = "anything\n\nother\ntest\ntest2";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(6, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">test</span>,anything]", rows.get(0).toString());
        assertEquals("[CHANGE,anything<span class=\"editOldInline\"> </span>,]", rows.get(1).toString());
        assertEquals("[CHANGE,<span class=\"editOldInline\"> </span>,]", rows.get(2).toString());
        assertEquals("[EQUAL,other,other]", rows.get(3).toString());
        assertEquals("[INSERT,<span class=\"editNewInline\">test</span>,test]", rows.get(4).toString());
        assertEquals("[INSERT,<span class=\"editNewInline\">test2</span>,test2]", rows.get(5).toString());
    }

    @Test
    public void testGeneratorWithMergeByWord4() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("Test"), Arrays.asList("ester"));
        print(rows);

        assertEquals(1, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">Test</span><span class=\"editNewInline\">ester</span>,ester]", rows.get(0).toString());
    }

    @Test
    public void testGeneratorWithMergeByWord5() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .columnWidth(80)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("Test feature"), Arrays.asList("ester feature best"));
        print(rows);

        assertEquals(1, rows.size());
        assertEquals("[CHANGE,<span class=\"editOldInline\">Test</span><span class=\"editNewInline\">ester</span> <br/>feature<span class=\"editNewInline\"> best</span>,ester feature best]", rows.get(0).toString());
    }

    @Test
    public void testSplitString() {
        List<String> list = DiffRowGenerator.splitStringPreserveDelimiter("test,test2", DiffRowGenerator.SPLIT_BY_WORD_PATTERN);
        assertEquals(3, list.size());
        assertEquals("[test, ,, test2]", list.toString());
    }

    @Test
    public void testSplitString2() {
        List<String> list = DiffRowGenerator.splitStringPreserveDelimiter("test , test2", DiffRowGenerator.SPLIT_BY_WORD_PATTERN);
        System.out.println(list);
        assertEquals(5, list.size());
        assertEquals("[test,  , ,,  , test2]", list.toString());
    }

    @Test
    public void testSplitString3() {
        List<String> list = DiffRowGenerator.splitStringPreserveDelimiter("test,test2,", DiffRowGenerator.SPLIT_BY_WORD_PATTERN);
        System.out.println(list);
        assertEquals(4, list.size());
        assertEquals("[test, ,, test2, ,]", list.toString());
    }

    @Test
    public void testGeneratorExample1() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This is a test senctence."),
                Arrays.asList("This is a test for diffutils."));

        System.out.println(rows.get(0).getOldLine());

        assertEquals(1, rows.size());
        assertEquals("This is a test ~senctence~**for diffutils**.", rows.get(0).getOldLine());
    }

    @Test
    public void testGeneratorExample2() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This is a test senctence.", "This is the second line.", "And here is the finish."),
                Arrays.asList("This is a test for diffutils.", "This is the second line."));

        System.out.println("|original|new|");
        System.out.println("|--------|---|");
        for (DiffRow row : rows) {
            System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
        }

        assertEquals(3, rows.size());
        assertEquals("This is a test ~senctence~.", rows.get(0).getOldLine());
        assertEquals("This is a test **for diffutils**.", rows.get(0).getNewLine());
    }

    @Test
    public void testGeneratorUnchanged() {
        String first = "anything \n \nother";
        String second = "anything\n\nother";

        DiffRowGenerator generator = DiffRowGenerator.create()
                .columnWidth(5)
                .reportLinesUnchanged(true)
                .build();
        List<DiffRow> rows = generator.generateDiffRows(split(first), split(second));
        print(rows);

        assertEquals(3, rows.size());
        assertEquals("[CHANGE,anything ,anything]", rows.get(0).toString());
        assertEquals("[CHANGE, ,]", rows.get(1).toString());
        assertEquals("[EQUAL,other,other]", rows.get(2).toString());
    }

    @Test
    public void testGeneratorIssue14() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffBySplitter(line -> DiffRowGenerator.splitStringPreserveDelimiter(line, Pattern.compile(",")))
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("J. G. Feldstein, Chair"),
                Arrays.asList("T. P. Pastor, Chair"));

        System.out.println(rows.get(0).getOldLine());

        assertEquals(1, rows.size());
        assertEquals("~J. G. Feldstein~**T. P. Pastor**, Chair", rows.get(0).getOldLine());
    }

    @Test
    public void testGeneratorIssue15() throws IOException {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true) //show the ~ ~ and ** ** symbols on each difference
                .inlineDiffByWord(true) //show the ~ ~ and ** ** around each different word instead of each letter
                //.reportLinesUnchanged(true) //experiment
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();

        List<String> listOne = Files.lines(new File("target/test-classes/mocks/issue15_1.txt").toPath())
                .collect(toList());

        List<String> listTwo = Files.lines(new File("target/test-classes/mocks/issue15_2.txt").toPath())
                .collect(toList());

        List<DiffRow> rows = generator.generateDiffRows(listOne, listTwo);

        assertEquals(9, rows.size());

        for (DiffRow row : rows) {
            System.out.println("|" + row.getOldLine() + "| " + row.getNewLine() + " |");
            if (!row.getOldLine().startsWith("TABLE_NAME")) {
                assertTrue(row.getNewLine().startsWith("**ACTIONS_C16913**"));
                assertTrue(row.getOldLine().startsWith("~ACTIONS_C1700"));
            }
        }
    }

    @Test
    public void testGeneratorIssue22() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        String aa = "This is a test senctence.";
        String bb = "This is a test for diffutils.\nThis is the second line.";
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList(aa.split("\n")),
                Arrays.asList(bb.split("\n")));

        assertEquals("[[CHANGE,This is a test ~senctence~.,This is a test **for diffutils**.], [CHANGE,,**This is the second line.**]]",
                rows.toString());

        System.out.println("|original|new|");
        System.out.println("|--------|---|");
        for (DiffRow row : rows) {
            System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
        }
    }

    @Test
    public void testGeneratorIssue22_2() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        String aa = "This is a test for diffutils.\nThis is the second line.";
        String bb = "This is a test senctence.";
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList(aa.split("\n")),
                Arrays.asList(bb.split("\n")));

        assertEquals("[[CHANGE,This is a test ~for diffutils~.,This is a test **senctence**.], [CHANGE,~This is the second line.~,]]",
                rows.toString());
    }

    @Test
    public void testGeneratorIssue22_3() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        String aa = "This is a test senctence.";
        String bb = "This is a test for diffutils.\nThis is the second line.\nAnd one more.";
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList(aa.split("\n")),
                Arrays.asList(bb.split("\n")));

        assertEquals("[[CHANGE,This is a test ~senctence~.,This is a test **for diffutils**.], [CHANGE,,**This is the second line.**], [CHANGE,,**And one more.**]]",
                rows.toString());
    }

    @Test
    public void testGeneratorIssue41DefaultNormalizer() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("<"), Arrays.asList("<"));
        assertEquals("[[EQUAL,&lt;,&lt;]]", rows.toString());
    }

    @Test
    public void testGeneratorIssue41UserNormalizer() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .lineNormalizer(str -> str.replace("\t", "    "))
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("<"), Arrays.asList("<"));
        assertEquals("[[EQUAL,<,<]]", rows.toString());
        rows = generator.generateDiffRows(Arrays.asList("\t<"), Arrays.asList("<"));
        assertEquals("[[CHANGE,    <,<]]", rows.toString());
    }

    @Test
    public void testGenerationIssue44reportLinesUnchangedProblem() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .reportLinesUnchanged(true)
                .oldTag(f -> "~~")
                .newTag(f -> "**")
                .build();
        List<DiffRow> rows = generator.generateDiffRows(Arrays.asList("<dt>To do</dt>"), Arrays.asList("<dt>Done</dt>"));
        assertEquals("[[CHANGE,<dt>~~T~~o~~ do~~</dt>,<dt>**D**o**ne**</dt>]]", rows.toString());
    }

    @Test
    public void testIgnoreWhitespaceIssue66() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(true)
                .mergeOriginalRevised(true)
                .oldTag(f -> "~") //introduce markdown style for strikethrough
                .newTag(f -> "**") //introduce markdown style for bold
                .build();

        //compute the differences for two test texts.
        //CHECKSTYLE:OFF
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This\tis\ta\ttest."),
                Arrays.asList("This is a test"));
        //CHECKSTYLE:ON

        assertEquals("This    is    a    test~.~", rows.get(0).getOldLine());
    }

    @Test
    public void testIgnoreWhitespaceIssue66_2() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(true)
                .mergeOriginalRevised(true)
                .oldTag(f -> "~") //introduce markdown style for strikethrough
                .newTag(f -> "**") //introduce markdown style for bold
                .build();

        //compute the differences for two test texts.
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This  is  a  test."),
                Arrays.asList("This is a test"));

        assertEquals("This  is  a  test~.~", rows.get(0).getOldLine());
    }

    @Test
    public void testIgnoreWhitespaceIssue64() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(true)
                .mergeOriginalRevised(true)
                .oldTag(f -> "~") //introduce markdown style for strikethrough
                .newTag(f -> "**") //introduce markdown style for bold
                .build();

        //compute the differences for two test texts.
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("test\n\ntestline".split("\n")),
                Arrays.asList("A new text line\n\nanother one".split("\n")));

        assertThat(rows).extracting(item -> item.getOldLine())
                .containsExactly("~test~**A new text line**",
                        "",
                        "~testline~**another one**");
    }

    @Test
    public void testReplaceDiffsIssue63() {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .mergeOriginalRevised(true)
                .oldTag(f -> "~") //introduce markdown style for strikethrough
                .newTag(f -> "**") //introduce markdown style for bold
                .processDiffs(str -> str.replace(" ", "/"))
                .build();

        //compute the differences for two test texts.
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This  is  a  test."),
                Arrays.asList("This is a test"));

        assertEquals("This~//~**/**is~//~**/**a~//~**/**test~.~", rows.get(0).getOldLine());
    }
}
