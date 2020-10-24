




SendOTP Android Sdk!
===================
## For Androidx and Above use this library and [for older sdk 28 and blow see here](https://github.com/MSG91/sendotp-android/tree/version_1.2)
**This Library Supports Androidx and older versions up to (SDK-16):**

The  **SendOtp** Verification SDK makes verifying phone numbers easy. SDK supports the verification of phone numbers via **SMS & Calls**.




----------

Getting started
===============

Gradle
------

Just add below dependency in project's app level build.gradle file

    dependencies {
    ...
     implementation ‘com.msg91.sendotpandroid.library:library:1.4’
    ...
    }  
    
    
Also, add below url in project's  project level build.gradle file   
    
    maven{
      url "https://dl.bintray.com/walkover/Android-Libs"
    }
    
**New Update**: Auto verify if same mobile number sim is insrted in device.

Maven
------
grab via Maven:

    <dependency>
      <groupId>com.msg91.sendotpandroid.library</groupId>
      <artifactId>library</artifactId>
      <version>1.3.8</version>
      <type>pom</type>
    </dependency>
Ivy
------
grab via Ivy:

    <dependency org='com.msg91.sendotpandroid.library' name='library' rev='1.3.8'>
      <artifact name='library' ext='pom' ></artifact>
    </dependency>

> -Login or create account at [MSG91]([https://control.msg91.com/signup/sendotp](https://control.msg91.com/signup/sendotp)) to use sendOTP services.

#### <i class="icon-file"></i> Get your authKey

After login at [MSG91](https://control.msg91.com/) </i> follow below steps to get your **authkey**




> - Select **API** option available on panel.
> - If you are first time user then generate new authkey.
> - copy authKey & keep it enable

#### <i class="icon-book"></i> Usage

>  initialize'**SendOTP**' in your Application class.

    public class AppController extends Application {
        @Override
      public void onCreate() {
               super.onCreate();
              SendOTP.initializeApp(this,"authKey");        //initialization
      }
    }


>  implement '**VerificationListener**' in your class & override below result callback.

    @Override
	public void onSendOtpResponse(final SendOTPResponseCode responseCode, final String message) {
    runOnUiThread(new Runnable() {
        @Override
	  public void run() {
            Log.e(TAG, "onSendOtpResponse: " + responseCode.getCode() + "=======" + message);
            if (responseCode == SendOTPResponseCode.DIRECT_VERIFICATION_SUCCESSFUL_FOR_NUMBER || responseCode == SendOTPResponseCode.OTP_VERIFIED) {
                //otp verified OR direct verified by send otp 2.O
		    } else if (responseCode == SendOTPResponseCode.READ_OTP_SUCCESS) {
                //Auto read otp from sms successfully
			   // you can get otp form message filled
		    } else if (responseCode == SendOTPResponseCode.SMS_SUCCESSFUL_SEND_TO_NUMBER || responseCode == SendOTPResponseCode.DIRECT_VERIFICATION_FAILED_SMS_SUCCESSFUL_SEND_TO_NUMBER)
		    {
                // Otp send to number successfully
			} else {
                //exception found
			}
        }
	 });
	}

Build Your requirements by initialize builder with pass country code and mobile number.
Optional Parameters are gose in blow method.

    new SendOTPConfigBuilder()
            .setCountryCode(countryCode)
            .setMobileNumber(phoneNumber)
            .setVerifyWithoutOtp(true)//direct verification while connect with mobile network
			.setAutoVerification(VerificationActivity.this)//Auto read otp from Sms And Verify
			.setSenderId("ABCDEF")
            .setMessage("##OTP## is Your verification digits.")
            .setOtpLength(OTP_LNGTH)
            .setVerificationCallBack(this).build();


**Sending OTP / StartVerification** to Number by using above configuration.

    SendOTP.getInstance().getTrigger().initiate();

manually **verifying OTP**

    SendOTP.getInstance().getTrigger().verify(otp);
**resend OTP** by voice or text .

    SendOTP.getInstance().getTrigger().resend(RetryType.VOICE);
   **OR**


    SendOTP.getInstance().getTrigger().resend(RetryType.TEXT);



**customize message text** :

##OTP##  is use for default OTP genrated from sdk

    .message("##OTP## is Your verification digits.")
**OR**
genrate your otp and set in parameter

    String OTP = "1234";

and use blow method

    .otp(OTP )
    .message(OTP +" is Your verification digits.")

**Unicode** : To show unicode sms set true in unicode parameter.

    .unicode(true)

**Quick integration video [here it is](https://www.youtube.com/watch?v=LSHhzTuj2gM)**

Optional Parameters
------
> - **setMessage**("##OTP## with your Custom OTP message.") [for custom OTP message]
>- **setOtpExpireInMinute**(5) [long param ,default value is one day]
>- **setSenderId**("SENDOTP")
>- **setOtp**("1234") [use your OTP code]
>- **setOtpLength**("4") [custom OTP length]
>- **setUnicodeEnable**(false) [use unicode (or other languages)]
>- **setVerifyWithoutOtp** (true) [direct verification while connect with mobile network]
>- **setOtpHits** (5) [number of otp request per number]
>- **setOtpHitsTimeOut** (0L) [number of otp request time out reset in milliseconds default is 24 hours]
>- **setAutoVerification** (ActivityContext_here) [number of otp request per number]
>
<img src="https://user-images.githubusercontent.com/47854558/71350020-5c2d0d80-2596-11ea-8ba8-0bfca83b3602.png" width="270">    <img src="https://user-images.githubusercontent.com/47854558/71351134-ec6c5200-2598-11ea-8da3-b38c88c02dcd.png" width="270">  <img src="https://user-images.githubusercontent.com/47854558/71350022-5c2d0d80-2596-11ea-9b77-3aa2d0a53e8f.png" width="270">

License
=======

    Copyright 2020 MSG91

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
