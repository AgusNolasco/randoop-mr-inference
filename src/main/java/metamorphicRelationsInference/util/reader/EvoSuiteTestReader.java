package metamorphicRelationsInference.util.reader;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import randoop.DummyVisitor;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceParseException;
import randoop.test.DummyCheckGenerator;
import randoop.util.sequence.SequenceParser;

public class EvoSuiteTestReader {

  public static List<ExecutableSequence> readFromFile(String pathToFile) {
    return readFromFile(new File(pathToFile));
  }

  private static List<ExecutableSequence> readFromFile(File file) {
    List<ExecutableSequence> sequences = new ArrayList<>();
    CompilationUnit cu = getCompilationUnit(file);
    ClassOrInterfaceDeclaration clazz = getClass(cu);
    for (MethodDeclaration md : getMethods(clazz)) {
      List<String> imports = getClassImports(cu);
      String code = replaceObjectConstruction(getMethodCode(md));
      Sequence seq = getSeqFromCode(code, imports, clazz.getNameAsString());
      sequences.add(executeSeq(seq));
    }
    return sequences;
  }

  private static ClassOrInterfaceDeclaration getClass(CompilationUnit cu) {
    return cu.getType(0).asClassOrInterfaceDeclaration();
  }

  private static CompilationUnit getCompilationUnit(File f) {
    JavaParser jp = new JavaParser();
    try {
      ParseResult<CompilationUnit> result = jp.parse(f);
      if (result.getResult().isPresent()) {
        return result.getResult().get();
      }
      throw new IllegalStateException("Compilation Unit is not present");
    } catch (FileNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static String getMethodCode(MethodDeclaration md) {
    if (!md.getBody().isPresent()) {
      throw new RuntimeException("Method declaration body is not present");
    }
    NodeList<Statement> statements = md.getBody().get().getStatements();
    return new ArrayList<>(statements)
        .stream().map(Objects::toString).collect(Collectors.joining("\n"));
  }

  private static List<String> getClassImports(CompilationUnit cu) {
    return cu.getImports().stream().map(imp -> imp.getNameAsString()).collect(Collectors.toList());
  }

  private static List<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration clazz) {
    return clazz.getMembers().stream().map(m -> (MethodDeclaration) m).collect(Collectors.toList());
  }

  private static String replaceObjectConstruction(String code) {
    // TODO: Maybe we could try to get other objects, instead of only replace them with strings
    return code.replaceAll("Object\\(\\)", "String()");
  }

  private static Sequence getSeqFromCode(String code, List<String> imports, String forClass) {
    try {
      return SequenceParser.codeToSequence(code, imports, forClass);
    } catch (SequenceParseException e) {
      throw new RuntimeException("Could not generate sequence for code: \n\n" + code);
    }
  }

  private static ExecutableSequence executeSeq(Sequence seq) {
    ExecutableSequence execSeq = new ExecutableSequence(seq);
    execSeq.execute(new DummyVisitor(), new DummyCheckGenerator());
    return execSeq;
  }
}
