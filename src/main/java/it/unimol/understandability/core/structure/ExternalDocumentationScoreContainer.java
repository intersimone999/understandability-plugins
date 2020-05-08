package it.unimol.understandability.core.structure;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import it.unimol.understandability.core.ProjectUtils;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import it.unimol.understandability.utils.CacheManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by simone on 03/02/17.
 */
public class ExternalDocumentationScoreContainer {
    private static final String CACHE_CLASS_NAMES_FILE = ".extClassNames";

    private static final Map<Project, ExternalDocumentationScoreContainer> instances = new HashMap<>();

    public static ExternalDocumentationScoreContainer getInstance(Project project) {
        if (!instances.containsKey(project)) {
            instances.put(project, new ExternalDocumentationScoreContainer(project));
        }

        return instances.get(project);
    }

    private final Project project;

    private static Map<String, Double> externalScoreCacheLoaded;
    private final Map<String, Double> externalScoreCache;

    private final Map<String, Double> documentationScore;
    private final Map<String, Double> normalizedDocumentationScore;

    private ExternalDocumentationScoreContainer(Project project) {
        this.externalScoreCache = this.loadCache();

        this.project = project;

        this.documentationScore = new HashMap<>();
        this.normalizedDocumentationScore = new HashMap<>();
    }

    public Set<String> gatherComponents() throws IOException {
        return this.gatherComponents(null);
    }

    public Set<String> gatherComponents(ProgressIndicator indicator) throws IOException {
        CacheManager<String> cacheManager = CacheManager.getStringInstance();

        String cacheFilename = new File(this.project.getBaseDir().getCanonicalPath(), CACHE_CLASS_NAMES_FILE).getPath();
        Set<String> classNames = cacheManager.loadSet(cacheFilename);

        if (classNames.size() != 0)
            return classNames;

        Set<PsiElement> toAnalyse = ProjectUtils.getInstance().getAllProjectElements(this.project, indicator);
        double done = 0.0;
        for (PsiElement element : toAnalyse) {
            done++;
            if (indicator != null) {
                indicator.setFraction(done / toAnalyse.size());
            }

            if (!(element instanceof PsiClass))
                continue;

            PsiClass psiClass = (PsiClass) element;
            if (PsiUtils.isSourceElement((PsiClass) element) || psiClass.getQualifiedName() == null)
                continue;

            if (classNames.add(psiClass.getQualifiedName()))
                cacheManager.addCacheSetEntry(cacheFilename, psiClass.getQualifiedName());
        }

        return classNames;
    }

    public void downloadAll() throws IOException {
        this.downloadAll(false, null);
    }

    public void downloadAll(boolean force) throws IOException {
        this.downloadAll(force, null);
    }

    public void downloadAll(boolean force, ProgressIndicator indicator) throws IOException {
        if (this.documentationScore != null && this.normalizedDocumentationScore != null && !force)
            return;

        this.documentationScore.clear();
        this.normalizedDocumentationScore.clear();

        Set<String> classNames = this.gatherComponents();

        double maxScore = -Double.MAX_VALUE;
        double done = 0.0;
        for (String classQualifiedName : classNames) {
            double score = this.getScore(classQualifiedName);

            if (score > maxScore)
                maxScore = score;

            this.documentationScore.put(classQualifiedName, score);

            done++;
            if (indicator != null)
                indicator.setFraction(done / classNames.size());
        }

        for (Map.Entry<String, Double> entry : this.documentationScore.entrySet()) {
            this.normalizedDocumentationScore.put(entry.getKey(), entry.getValue() / maxScore);
        }
    }

    public double getNormalizedScoreFor(PsiClass psiClass) {
        if (!this.normalizedDocumentationScore.containsKey(psiClass.getQualifiedName())) {
            return Double.NaN;
        }

        return this.normalizedDocumentationScore.get(psiClass.getQualifiedName());
    }

    public double getScore(PsiClass psiClass) {
        if (!this.documentationScore.containsKey(psiClass.getQualifiedName())) {
            return Double.NaN;
        }

        return this.documentationScore.get(psiClass.getQualifiedName());
    }

    private double getScore(String name) {
        if (this.externalScoreCache.containsKey(name))
            return this.externalScoreCache.get(name);

        Map<String, Object> queryString = new HashMap<>();
        queryString.put("q", name);
        queryString.put("accepted", "True");
        queryString.put("tagged", "java");
        queryString.put("qualifiedName", "\"how to\"");
        queryString.put("site", "stackoverflow");
        queryString.put("pagesize", "100");
        queryString.put("key", UnderstandabilityPreferences.getStackOverflowApiKey());

        try {
            double sumScore = 0;

            boolean hasMore = true;
            int page = 1;
            while (hasMore) {
                queryString.put("page", page);
                HttpResponse<JsonNode> response = null;
                try {
                    GetRequest request = Unirest.get("https://api.stackexchange.com/2.2/search/advanced");
                    request.queryString(queryString);
                    response = request.asJson();
                } catch (Exception e) {
                    System.out.println();
                    throw new UnirestException("");
                }

                JsonNode json = response.getBody();
                JSONObject object = json.getObject();

                JSONArray items = object.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);

                    int score = item.getInt("score") + 1;

                    sumScore += score;
                }
                page++;
                hasMore = object.getBoolean("has_more");
            }

            this.updateCache(name, sumScore);
            return sumScore;
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateCache(String key, Double value) {
        externalScoreCache.put(key, value);

        Writer fileWriter = null;
        BufferedWriter output = null;
        try {
            fileWriter = new FileWriter(UnderstandabilityPreferences.getStackOverflowCacheFile(), true);
            output = new BufferedWriter(fileWriter);

            output.write(key + "," + value + "\n");

            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (output != null)
                    output.close();

                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, Double> loadCache() {
        if (externalScoreCacheLoaded != null)
            return externalScoreCacheLoaded;

        File file = new File(UnderstandabilityPreferences.getStackOverflowCacheFile());
        try {
            if (!file.exists()) {
                file.createNewFile();

                externalScoreCacheLoaded = new HashMap<>();

                return externalScoreCacheLoaded;
            }

            externalScoreCacheLoaded = CacheManager.getNumericInstance().loadMap(UnderstandabilityPreferences.getStackOverflowCacheFile());

            return externalScoreCacheLoaded;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
