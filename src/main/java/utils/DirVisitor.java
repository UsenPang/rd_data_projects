package utils;


import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

public interface DirVisitor {
    FileVisitResult visitor(File dir, BasicFileAttributes attrs);
}
