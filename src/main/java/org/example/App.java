package org.example;

import org.jsfr.json.JacksonParser;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.path.JsonPath;
import org.jsfr.json.provider.JacksonProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.jsfr.json.compiler.JsonPathCompiler.compile;

/**
 * Hello world!
 */
public class App {
    private static final JsonSurfer surfer = new JsonSurfer(JacksonParser.INSTANCE, JacksonProvider.INSTANCE);

    public static void main(String[] args) throws IOException {

        var folder = "/Users/a19045391/IdeaProjects/edu/recovery-ia/src/main/resources/divided";

        var files = Files.walk(Path.of(folder))
                .filter(file -> !folder.equals(file.toString()))
                .sorted()
                .peek(System.out::println)
                .collect(Collectors.toList());

        var d = new FileDownloader(files);
        var is = new SequenceInputStream(d);
        Iterator<Object> iterator = surfer.iterator(is, compile("$[*]"));
        d.setIterator(iterator);

        StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false)
                .map(Object::toString)
                .forEach(System.out::println);

    }

    private static class FileDownloader implements Enumeration<InputStream> {

        private int step = -1;
        private AtomicReference<byte[]> current;
        private List<Path> files;
        private Iterator<Object> iterator;

        public FileDownloader(List<Path> files) {
            this.files = files;
        }

        @Override
        public boolean hasMoreElements() {

            if (++step < files.size()) {
                try {



                    current = new AtomicReference<>(Files.readAllBytes(files.get(step)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public InputStream nextElement() {
            var currentL = current.get();
            current = null;
            return new ByteArrayInputStream(currentL);
        }

        public void setIterator(Iterator<Object> iterator) {
            this.iterator = iterator;
        }
    }
}
