//
//  OGChangePinDelegate.h
//  OneginiSDKiOS
//
//  Created by Eduard on 20-02-15.
//  Copyright (c) 2015 Onegini. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 This protocol informs the client about the result of the PIN change request.
 Clients must also implement the OGPinValidationDelegate protocol.
 */
@protocol OGChangePinDelegate <NSObject>

- (void)invalidCurrentPin DEPRECATED_ATTRIBUTE;

/**
 The access grant or refresh token provided by the client is invalid.
 
 @param remaining, the number of remaining attempts before the token becomes invalid
 */
- (void)invalidCurrentPin:(NSUInteger)remaining;

@required

- (void)pinChanged;
- (void)pinChangeError;
- (void)pinChangeError:(NSError *)error;

@end