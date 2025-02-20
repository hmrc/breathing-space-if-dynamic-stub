
# breathing-space-if-dynamic-stub

The dynamic stub is available locally as the default in the sm profile BREATHING_SPACE_ALL  and BREATHING_SPACE_ACCEPTANCE_ALL , development and QA environments

The service allows a user or tests to dynamically add data so that it may be used for testing purposes. It exposes several endpoints described below

If nothing is set up dynamically for the following ninos then for the /memorandum endpoint some hard-coded static data is used:
- AS000001: Status 200 with json body holding {breathingSpaceIndicator: true}
- AS000002: Status 200 with json body holding {breathingSpaceIndicator: false}
- AA000333: Status 200 with json body holding {breathingSpaceIndicator: true}
- AS000003: Status 422 with response code UNKNOWN_DATA_ITEM
- AS000004: Status 502 with response code BAD_GATEWAY


|Usage| Endpoint      |Data|
|-----|---------------|----|
|Insert a single individual details, including debt information and period information| POST: /single | https://github.com/hmrc/breathing-space-if-dynamic-stub/blob/main/app/uk/gov/hmrc/breathingspaceifstub/model/IndividualInRequest.scala#L21|
|Insert multiple individuals details, including debt information and period information|POST: /bulk|https://github.com/hmrc/breathing-space-if-dynamic-stub/blob/main/app/uk/gov/hmrc/breathingspaceifstub/model/IndividualInRequest.scala#L29|
|Update an individuals information|PUT: /{NINO}|Supply the nino of the user you want to replace the details for using https://github.com/hmrc/breathing-space-if-dynamic-stub/blob/main/app/uk/gov/hmrc/breathingspaceifstub/model/IndividualInRequest.scala#L21|
|Delete an individuals information|DELETE: /{NINO}|Supply the nino of the user you want deleted in the URL|
|Delete all individuals information|DELETE: /wipe-all|No data needs supplied|
|List all stored ninos|GET: /ninos|No data needs supplied|
|Count all records|GET: /count|No data needs supplied|
|Does record exist|GET: /{NINO}|Supply the nino of the record you wish to check|
|Overview of all data in the stub|GET: /overview|No data needs supplied|
|Retrieve UTR for nino|GET: /utr/{NINO}|Supply the nino of the record you with to find the UTR for|
|Add multiple coding out debts|POST: /{NINO}/{PERIOD_ID}/codingout/bulk|Supply the nino and period id of the record you wish to update in the URL along with the following in the body|
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
