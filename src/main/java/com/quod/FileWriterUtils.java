package com.quod;

import com.quod.postprocess.Repo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class FileWriterUtils {
    static private String PATH = "./data/";

    public static void saveToCSV(List<Repo> repoList){

        File directory = new File(PATH);
        if (! directory.exists()){
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        try (PrintWriter writer = new PrintWriter(new File(PATH + "test.csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append("index,");
            sb.append("id");
            sb.append(',');
            sb.append("Name");
            sb.append(',');
            sb.append("issueOpenScore");
            sb.append(',');
            sb.append("pullRequestMergeScore");
            sb.append(',');
            sb.append("commitRatioScore");
            sb.append(',');
            sb.append("totalScore");
            sb.append('\n');
            Integer count = 1;
            for (Repo repo : repoList.subList(0, 1000)){
                sb.append(count.toString());
                sb.append(',');
                sb.append(repo.getRepoId());
                sb.append(',');
                sb.append(repo.getName());
                sb.append(',');
                sb.append(repo.getIssueDurScore());
                sb.append(',');
                sb.append(repo.getPullEvenDurScore());
                sb.append(',');
                sb.append(repo.getRatioCommScore());
                sb.append(',');
                sb.append(repo.getTotalScore());
                sb.append('\n');
                count += 1;
            }

            writer.write(sb.toString());

            System.out.println("done!");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
