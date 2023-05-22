package uk.md.MaternityCalculationsAPI.Controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.md.MaternityCalculationsAPI.BusinessLogic.BusiestDayLogic;
import uk.md.MaternityCalculationsAPI.BusinessLogic.DischargedQuickLogic;
import uk.md.MaternityCalculationsAPI.Models.PatientCustom;
import uk.md.MaternityCalculationsAPI.Models.Entities.Admission;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequestMapping("api/")
@Api(value = "Responsible for all business logic calculations")
public class AdmissionsController {
    private final GetApiLists _httpHandler = new GetApiLists();

    @GetMapping("PatientsSeen/{id}")
    @ApiOperation(value = "Based on EmployeeID: Returns a list unique patients that have visited the staff (Model same as Discharged Quickly). Each Allocation in Allocations, loops through and matches EmployeeID. If it is found it will get Admission by ID and look at PatientID. If the Patient is unique add to List of Model.")
    public String PatientsSeenByStaff(@RequestParam("id") int EmployeeID) {
        return null;
    }

    @GetMapping("DischargedQuick")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Returns a list of patients who are discharged within 3 days of admission")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Bad Request: Error Calculating Quick Discharge Patients List"),
                    @ApiResponse(code = 200, message = "Success: List of Patients discharged within 3 days")
            })
    public ResponseEntity<List<PatientCustom>> DischargedQuick() throws IOException, InterruptedException {
        HttpResponse<String> res = _httpHandler.getAdmissionsList();

        // All Responses are JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Checking if Successful or Error
        if (res.statusCode() == 200) {
            List<Admission> allAdmissions = _httpHandler.parseAdmissionList(res);
            DischargedQuickLogic logicObj = new DischargedQuickLogic();
            
            List<PatientCustom> allDischargedQuick = logicObj.calculateDischargedQuick(allAdmissions);

            return new ResponseEntity<>(allDischargedQuick, headers, HttpStatus.OK);
        }
        else if (res.statusCode() == 404) {
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("AvgDurationByStaff")
    @ApiOperation(value = "Based on EmployeeID, Returns the mean calculated avg duration of recovery time/stay of all their patients")
    public String AvgDurationByStaff() {
        return null;
    }

    // Provide Week: Returns the busiest day based on Mode
    // Single Busiest Day: ["Monday"] (If Monday is busiest)
    // If the busiest days are equal: Return Both Days
    // ["Monday", "Wednesday"] (If Mon & Wed are Both Busiest & Equal)
    @GetMapping("BusiestDayOfWeek")
    @ApiOperation(value = "Returns the calculated avg busiest day of the week",
            notes = "Based on the Maternity '/Admissions' Endpoint: Loops through the List and Calculates the frequency of days in a hash table (e.g 'Mon: 4, Tues:2, Wed: 10...'). Then Returns the busiest day or days. Example Responses ['Monday'] if only Mon is Busiest or ['Wednesday','Thursday'] if Wed & Thus are equal & Busiest")
    public ResponseEntity<List<String>> BusiestDayOfWeek() throws IOException, InterruptedException {
        // All Responses are JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // getting all allocations
        HttpResponse<String> res = _httpHandler.getAdmissionsList();

        // Checking if Successful or Error
        if (res.statusCode() == 200) {
            // Parsing Admissions
            List<Admission> allAdmissions = _httpHandler.parseAdmissionList(res);

            // Run Busiest Day Logic
            BusiestDayLogic logicObj = new BusiestDayLogic();

            return new ResponseEntity<>(logicObj.calculateBusiestDay(allAdmissions), headers, HttpStatus.OK);
        }
        else if (res.statusCode() == 404) {
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
    }
}
