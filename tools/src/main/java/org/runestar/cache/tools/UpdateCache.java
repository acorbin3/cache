package org.runestar.cache.tools;

import org.runestar.cache.format.disk.DiskCache;
import org.runestar.cache.format.net.NetCache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class UpdateCache {

    public static void main(String[] args) throws IOException {
        var start = Instant.now();

        try (var net = NetCache.connect(new InetSocketAddress("oldschool7.runescape.com", 43594), 177);
             var disk = DiskCache.open(Paths.get(".cache"))) {
            net.update(disk).join();
        }

        System.out.println(Duration.between(start, Instant.now()));
    }
}
