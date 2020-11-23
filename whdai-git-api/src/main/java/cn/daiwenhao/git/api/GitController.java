package cn.daiwenhao.git.api;

import cn.daiwenhao.git.core.GitCore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("repository")
public class GitController {

    @Value("${repository}")
    private String repository;


    @GetMapping("/{repoGroup}/{repoName}/info/refs")
    public void getRepoInfo(@PathVariable String repoGroup, @PathVariable String repoName, String service, HttpServletResponse response) throws IOException, InterruptedException {
        String repositoryPath = String.format("%s/%s/%s", repository, repoGroup, repoName);
        GitCore.getRepoInfo(repositoryPath, service, response);
    }

    @PostMapping("/{repoGroup}/{repoName}/{service}")
    public void server(@PathVariable String repoGroup,
                       @PathVariable String repoName,
                       @PathVariable String service,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, InterruptedException {
        String repositoryPath = String.format("%s/%s/%s", repository, repoGroup, repoName);
        GitCore.server(repositoryPath, service, request, response);
    }
}
