
# Kosher Rise 
    Application for field employees to fill data regarding fields. This application will help them in recording various details for different fields.

    This application also supports mutiple language (english, telugu, hindi, marathi, gujrati, odia). Current the application only supports english, telugu, hindi.

There are 8 main modules in this application.
1. Farmer Registration.
2. Crop Data.
3. Polygon.
4. Pipe Installation.
5. Capture Aeration Event.
6. Farmer Benefits.
7. Reports.
9. Weather.


    Note --> All the ablove modules are connect to one another. i.e. Data needs to filled in one module so it can be visible in other modules as well.

# In depth 
    Lets start discussing what modules does what.


1. Farmer Registration :
    In farmer registration the employee will add the basic details of the farmer and also some details related to the plot/field.


2. Crop data : 
    Once the farmer onboarding is completed. Then the employee is ready to fill the data related to the crops on that particular field.


3. Polygon :
    Once the crop data is submitted and approved by the admin then the polygon can be filled for that particular field. In polygon mapping the employee will mark the area of the field with the help of google map. Once the area has been plotted then the data can be saved for futher process.


4. Pipe installation :
    After polygon plotting the pipes need to be installed on the field. It will help the employee to install the pipes at different places on that particular fields as we are using geofencing inorder to help the emplyee placcing the pipes in the correct location.


5. Capture Aeration Event :
    Once the pipe installation data has been approved by the admin then the emoployee will fill the aeration data. Here the employee will fill the data multiple times as the alternate wetting and drying process will happen. The employee will be filling the same data multipe times with few variation as the areation event takes few daya.


6. Farmer Benefits :
    Data of giving any benefits to the farmer will be recorded in this moduler.


7. Reports :
    If any data that has been rejected, approved or in pending will come here. Also the employee will be able to fill data of the rejected entried from here.

8. Weather :
    This module will show user all the data related to the current, future weather. 



# File Structure

-- PNP AWd
    |
    -- userapp
        |
        -- adapters (All the adapters for recyclerview are in this directory).
        |
        -- awd (Capture Aeration Event module is present here)
            |
            -- AerationActivity
            -- AerationImageActivity
            -- AerationMapActivity
        -- cropdata
            |
            -- CropActivity
        -- cropintellix
            |
            -- DashboardActivity
            -- LanguageSelectActivity
            -- MainActivity
            -- PrivacyActivity
            -- ProfileActivity
            -- SignInActivity
            -- SignUpActivity
            -- SplashScreen
            -- TNCActivity
        -- farmerbenefit
            |
            --FarmerBenefitActivity
        -- farmeronboarding
            |
            -- FarmerOnboardingActivity
            -- LocationActivity
            -- PlotActivity
            -- StateActivity
            -- SubmitActivity
            -- TNCActivity
        -- modules
        -- network
            |
            -- ApiClient
            -- ApiInterface
        --pipeinstallation
            |
            -- Submitted
                |
                -- LandInfoSubmittedActivity
                -- LandInfoSubmittedPreviewActivity
                -- MapSubmittedActivity
            -- LandInfoActivity
            -- LandInfoPreviewActivity
            -- MapActivity
            -- PipeActivity
        -- reports
            |
            -- aeration_report
            -- benefit_report
            -- crop_report
            -- farmer_report
            -- pipe_report
        -- utils
            |
            -- Common
            -- Corner
            -- DecimalDigitalsInputFilter
            -- SignatureActivity
            -- SMSBroadcastReceiver


# Connection
    Farmer Onboarding
    The flow is as follows:
    StateActivity -> FarmerOnboardingActivity -> LocationActivity -> PlotActivity (Will be called multiple time based on the number of plots selected on the FarmerOnboardingActivity screen) -> TNCActivity -> SubmitActivity

    Crop Data
    The flow is as follows:
    CropActivity (Will be called multiple time based on the number of plots selected while farmer onboarding)

    Polygon
    The flow is as follows:  
    PipeActivity -> MapActivity -> LandInfoActivity

    Pipe Installation
    The flow is as follows: 
    PipeActivity -> MapSubmittedActivity -> LandInfpSubmittedActivity -> LandInfoSubmittedPreviewActivity

    Capture Aeration Event
    The flow is as follows: 
    AerationActivity -> AerationMapActivity -> AerationImageActivity

    Farmer Benefit
    The flow is as follows: 
    FarmerBenefitActivity (Only one screen)
## Features

- Light/dark mode toggle
- Multi Language
- Weather Forecast

