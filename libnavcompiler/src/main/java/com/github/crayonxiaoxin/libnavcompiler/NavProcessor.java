package com.github.crayonxiaoxin.libnavcompiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.crayonxiaoxin.libnavannotation.ActivityDestination;
import com.github.crayonxiaoxin.libnavannotation.FragmentDestination;
import com.google.auto.service.AutoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.WillClose;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.github.crayonxiaoxin.libnavannotation.ActivityDestination", "com.github.crayonxiaoxin.libnavannotation.FragmentDestination"})
public class NavProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private static final String OUTPUT_FILE_NAME = "destination.json";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> activityElements = roundEnv.getElementsAnnotatedWith(ActivityDestination.class);
        Set<? extends Element> fragmentElements = roundEnv.getElementsAnnotatedWith(FragmentDestination.class);
        if (!activityElements.isEmpty() || !fragmentElements.isEmpty()) {
            HashMap<String, JSONObject> destMap = new HashMap<>();
            handleDestination(activityElements, ActivityDestination.class, destMap);
            handleDestination(fragmentElements, FragmentDestination.class, destMap);
            // app/src/main/assets
            FileOutputStream fos = null;
            OutputStreamWriter writer = null;
            try {
                // 获取 CLASS_OUTPUT 路径
                FileObject res = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                String resPath = res.toUri().getPath();
                // 获取 app/ 路径
                String appPath = resPath.substring(0, resPath.indexOf("app") + 4);  // app/
                // 获取 app/src/main/assets 路径
                String assetsPath = appPath + "src/main/assets";
                File file = new File(assetsPath);
                if (!file.exists()) file.mkdir();
                // 新建 json 文件
                File outputFile = new File(file, OUTPUT_FILE_NAME);
                if (outputFile.exists()) outputFile.delete();
                outputFile.createNewFile();
                // json 写入文件
                String content = JSON.toJSONString(destMap);
                fos = new FileOutputStream(outputFile);
                writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                writer.write(content);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private void handleDestination(Set<? extends Element> elements, Class<? extends Annotation> annotationClazz, HashMap<String, JSONObject> destMap) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            String pageUrl = null;
            String clazzName = typeElement.getQualifiedName().toString();
            int id = Math.abs(clazzName.hashCode());
            boolean needLogin = false;
            boolean asStarter = false;
            boolean isFragment = false;
            Annotation annotation = typeElement.getAnnotation(annotationClazz);
            if (annotation instanceof ActivityDestination) {
                ActivityDestination destination = (ActivityDestination) annotation;
                pageUrl = destination.pageUrl();
                needLogin = destination.needLogin();
                asStarter = destination.asStarter();
                isFragment = false;
            } else if (annotation instanceof FragmentDestination) {
                FragmentDestination destination = (FragmentDestination) annotation;
                pageUrl = destination.pageUrl();
                needLogin = destination.needLogin();
                asStarter = destination.asStarter();
                isFragment = true;
            }
            if (destMap.containsKey(pageUrl)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "不同的页面不允许使用相同的pageUrl：" + clazzName);
            } else {
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("pageUrl", pageUrl);
                object.put("needLogin", needLogin);
                object.put("asStarter", asStarter);
                object.put("clazzName", clazzName);
                object.put("isFragment", isFragment);
                destMap.put(pageUrl, object);
            }
        }
    }
}
