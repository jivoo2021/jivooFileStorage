package com.palkar.jivooController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.palkar.jivooService.storageService;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/file")
public class s3Controller {
	@Autowired
	private storageService service;

	@Autowired
	private AmazonS3Client s3Client;

	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestParam(value = "file") MultipartFile file) {
		return new ResponseEntity<>(service.uploadFile(file), HttpStatus.OK);
	}

	@GetMapping("/download")
	public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam(value = "fileName") String fileName) {
		byte[] data = service.downloadFile(fileName);
		ByteArrayResource resource = new ByteArrayResource(data);
		return ResponseEntity.ok().contentLength(data.length).header("Content-type", "application/octet-stream")
				.header("Content-disposition", "attachment; filename=\"" + fileName + "\"").body(resource);
	}

	@DeleteMapping("/delete/{fileName}")
	public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
		return new ResponseEntity<>(service.deleteFile(fileName), HttpStatus.OK);
	}

	@GetMapping("/getObjects")
	public ResponseEntity<JSONObject> getObjectslistFromFolder(@RequestParam(value = "folderKey") String folderKey)
			throws IOException {
		List<String> objectsList = service.getObjectslistFromFolder(folderKey);
		List<String> objectsListFinal = new ArrayList<>();
		JSONObject allBooksUrlAndSizeObject = new JSONObject();
		for (int i = 0; i < objectsList.size(); i++) {
			String str = objectsList.get(i);
			int index = str.lastIndexOf('/');
			String nameOfBook = str.substring(index + 1, str.length() - 4);
			objectsListFinal.add(nameOfBook);
			String specificPdfUrl = "https://jivoo.s3.us-east-2.amazonaws.com/" + folderKey + "/" + nameOfBook + ".pdf";
			JSONObject specificPdfUrlObject = new JSONObject();
			Long pdfSize = getObjectSize(s3Client, "jivoo", folderKey + "/" + nameOfBook + ".pdf");
			Long megaByte = (long) 1048576;
			Long pdfSizeInMb = pdfSize / megaByte;
			specificPdfUrlObject.put("size", pdfSizeInMb + " MB");
			specificPdfUrlObject.put("url", specificPdfUrl);
			allBooksUrlAndSizeObject.put(nameOfBook, specificPdfUrlObject);
		}
		return new ResponseEntity<>(allBooksUrlAndSizeObject, HttpStatus.OK);
	}

	@GetMapping("/getObjectsUnderBucket")
	public ResponseEntity<JSONObject> getAllObjectslistFromBucket() {
		List<String> objectsList = service.getAllObjectslistFromBucket();
		JSONObject folder = new JSONObject();
		folder.put("jivoo", objectsList.toString());
		return new ResponseEntity<>(folder, HttpStatus.OK);
	}

	@GetMapping("/getAllFolderKeys")
	public ResponseEntity<JSONObject> getAllFolderKeys() {
		JSONObject folderKeys = new JSONObject();
		JSONObject englishMedium = new JSONObject();
		JSONObject tamilMedium = new JSONObject();
		String[] englishMediumFolderKeys = new String[] { "Academics/English-Medium/9th",
				"Academics/English-Medium/10th", "Academics/English-Medium/11th", "Academics/English-Medium/12th" };
		String[] tamilMediumFolderKeys = new String[] { "Academics/Tamil-Medium/9th", "Academics/Tamil-Medium/10th",
				"Academics/Tamil-Medium/11th", "Academics/Tamil-Medium/12th" };
		for (int i = 0; i < 4; i++) {
			String key = i + 9 + "th";
			englishMedium.put(key, englishMediumFolderKeys[i]);
			tamilMedium.put(key, tamilMediumFolderKeys[i]);
		}
		folderKeys.put("English-Medium", englishMedium);
		folderKeys.put("Tamil-Medium", englishMedium);
		return new ResponseEntity<>(folderKeys, HttpStatus.OK);
	}

	public Long getObjectSize(AmazonS3Client amazonS3Client, String bucket, String key) throws IOException {
		return amazonS3Client.getObjectMetadata(bucket, key).getContentLength();
	}

}
