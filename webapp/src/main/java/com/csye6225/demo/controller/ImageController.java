package com.csye6225.demo.controller;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.csye6225.demo.exception.DataValidationException;
import com.csye6225.demo.pojo.Image;
import com.csye6225.demo.pojo.Recipe;
import com.csye6225.demo.repository.RecipeRepository;
import com.csye6225.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
public class ImageController {
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    AmazonS3 s3;
    String AWS_ACCESS_KEY = "AKIAWWNJIPHBGA5N4WAU";
    String AWS_SECRET_KEY = "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";
    String bucketName = "webapp.syriii.me";

    @PostMapping(path = "/v1/recipe/{id}/image", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @PathVariable("id") String id, HttpServletResponse response) throws IOException, JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Recipe newRecipe = recipeRepository.findById(id);
        if(newRecipe == null){
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }
        AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY,AWS_SECRET_KEY);
//        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("endpoint","region"))//endpoint,region请指定为NOS支持的（us-east-1:hz,us-east2:bj）
//                .build();
        s3 = new AmazonS3Client(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY));
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        //exist image
        if(newRecipe.getImage() != null){
            s3.deleteObject(bucketName, "upload/"+newRecipe.getImage().getFilename());
            newRecipe.setImage(null);
        }
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        Image newImage = new Image();
        newImage.setImageid(uuid);
        newRecipe.setImage(newImage);
        try {
            String bucketPath = bucketName + "/upload";
            String FileName = file.getOriginalFilename();
            String suffix = FileName.substring(FileName.lastIndexOf(".") + 1);
            if(!(suffix.equals("jpg") || suffix.equals("png") || suffix.equals("jpeg"))){
                System.out.println(suffix);
                throw new DataValidationException(getDatetime(), 400, "Bad Request", "Unavailable File Type");
            }
            String newName = newImage.getImageid()+"."+suffix;
            newImage.setFilename(newName);
            File uploadFile = convertFile(file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketPath, newName, uploadFile);
            s3.putObject(putObjectRequest);
//            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, FileName);
//            URL url = s3.generatePresignedUrl(urlRequest);
            String url = s3.getUrl(bucketName, "upload/" + newName).toExternalForm();

            //get metadata
            ObjectMetadata metadata = s3.getObjectMetadata(bucketName, "upload/" + newName);
//            System.out.println(metadata.getRawMetadata());
            newImage.setAccept_ranges(metadata.getRawMetadataValue("Accept-Ranges").toString());
            newImage.setContent_length(String.valueOf(metadata.getContentLength()));
            newImage.setContent_type(metadata.getContentType());
            newImage.setMd5(metadata.getETag());
            newImage.setLast_modified(metadata.getLastModified().toString());
            
            newImage.setUrl(url);
            newRecipe.setImage(newImage);
            recipeRepository.save(newRecipe);
            ObjectMapper mapper = new ObjectMapper();
            String newImageJSON = mapper.writeValueAsString(newImage);
            return new ResponseEntity<>(newImageJSON, HttpStatus.CREATED);
        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (AmazonClientException ace) {
            ace.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path = "/v1/recipe/{id}/image/{imageId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> DeleteObject(@PathVariable("imageId") String imageid, @PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Recipe newRecipe = recipeRepository.findById(id);
        if(newRecipe == null){
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }
        Image newImage = newRecipe.getImage();
        if(!newImage.getImageid().equals(imageid)){
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Image Not Found");
        }
        String filename = newImage.getFilename();
        s3 = new AmazonS3Client(new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY));
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        s3.deleteObject(bucketName, "upload/"+filename);

//        newImage.setFilename("");
//        newImage.setUrl("");
//        newImage.setImageid("");
//        newRecipe.setImage(newImage);
//        recipeRepository.save(newRecipe);
        newRecipe.setImage(null);
        recipeRepository.save(newRecipe);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(path = "/v1/recipe/{id}/image/{imageId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> GetObject(@PathVariable("imageId") String imageid, @PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Recipe newRecipe = recipeRepository.findById(id);
        if(newRecipe == null){
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Recipe Not Found");
        }
        Image newImage = newRecipe.getImage();
        if(!newImage.getImageid().equals(imageid)){
            throw new DataValidationException(getDatetime(), 404, "Not Found", "Image Not Found");
        }

        ObjectMapper mapper = new ObjectMapper();
        String imageJSON = mapper.writeValueAsString(newImage);
        return new ResponseEntity<>(imageJSON, HttpStatus.OK);
    }

    private File convertFile(MultipartFile multipart) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }

    public String getDatetime() {
        Date currentTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String dateString = format.format(currentTime);
        return dateString;
    }

}
