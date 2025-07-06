package org.codewithzea.notificationservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Template {
    private String id;
    private String name;
    private String content;

    public Template(String name, String content) {
        this.name = name;
        this.content = content;
        this.id = name.toLowerCase().replace(" ", "-");
    }
}