package com.mindex.challenge.dao;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Employee findByEmployeeId(String employeeId);
    /*
    @Override
    @Update("{ $addField: { compensation: \"$$this.compensation\" } }")
    <S extends Employee> S save(S entity);
*/
    /*
db.employee.aggregation([
    { $match: { employeeId: '16a596ae-edd3-4847-99fe-c4518e82c86f' } },
    {
        $graphLookup: {
            from: "employee",
            startWith: "$directReports",
            connectFromField: "directReports",
            connectToField: "employeeId",
            as: "directReportTree"
        }
    },
    {
        $project: {
    		employee: "$$ROOT",
    		numberOfReports: {
    			$size: "$directReportTree"
    		}
        }
    }
])
     */
    @Aggregation({
"""
{ $match: { employeeId: '?0' } }
""","""
{
    $graphLookup: {
        from: "employee",
        startWith: "$directReports",
        connectFromField: "directReports",
        connectToField: "employeeId",
        as: "directReportTree"
    }
}
""","""
{
    $project: {
		employee: "$$ROOT",
		numberOfReports: {
			$size: "$directReportTree"
		}
    }
}
"""
    })
    ReportingStructure getReportingStructureByEmployeeId(String employeeId);
}
