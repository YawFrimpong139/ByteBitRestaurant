package org.codewithzea.restaurantservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GZipServletFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String acceptEncoding = request.getHeader("Accept-Encoding");

        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            response.setHeader("Content-Encoding", "gzip");

            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream())) {
                GZipServletResponseWrapper gzipResponse = new GZipServletResponseWrapper(response, gzipOutputStream);
                filterChain.doFilter(request, gzipResponse);
                gzipResponse.finish();
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
