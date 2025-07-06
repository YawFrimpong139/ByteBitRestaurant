package org.codewithzea.notificationservice.service;


import org.codewithzea.notificationservice.exception.TemplateNotFoundException;
import org.codewithzea.notificationservice.model.Template;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplateService {
    private final Map<String, Template> templateStore = new HashMap<>();

    public TemplateService() {
        // Initialize with some templates
        templateStore.put("welcome", new Template("Welcome", "Dear {name}, welcome to our service!"));
        templateStore.put("reset-password", new Template("Password Reset", "Hi {name}, your reset code is {code}"));
    }

    public Optional<String> template(String templateId) {
        return Optional.ofNullable(templateStore.get(templateId))
                .map(Template::getContent);
    }

    public String processTemplate(String templateContent, Map<String, String> variables) {
        String content = templateContent;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return content;
    }

    public Template getTemplate(String templateId) {
        Template template = templateStore.get(templateId);
        if (template == null) {
            throw new TemplateNotFoundException("Template not found: " + templateId);
        }
        return template;
    }

    public void addTemplate(Template template) {
        templateStore.put(template.getId(), template);
    }
}
