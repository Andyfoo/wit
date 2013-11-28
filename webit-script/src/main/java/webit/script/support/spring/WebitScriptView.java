// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script.support.spring;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.AbstractTemplateView;

/**
 *
 * @author Zqq
 */
public class WebitScriptView extends AbstractTemplateView {

    private WebitScriptViewResolver resolver;

    public void setResolver(WebitScriptViewResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        model.put("request", request);
        model.put("response", response);
        this.resolver.render(getUrl(), model, request, response);
    }
}